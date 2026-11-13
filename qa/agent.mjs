import { spawn } from "node:child_process";
import { mkdir, writeFile, appendFile } from "node:fs/promises";
import { fileURLToPath } from "node:url";
import { join } from "node:path";
import { startApi } from "./api-process.mjs";

const root = fileURLToPath(new URL("../", import.meta.url));
const android = join(root, "android");
const windows = process.platform === "win32";
const gradle = process.env.PANACEA_GRADLE || (windows ? "gradlew.bat" : "./gradlew");
const all = ["syntax", "integration", "api-e2e", "android-unit", "android-e2e"];
const selected = process.argv.slice(2);
const stages = selected.length ? [...new Set(selected)] : all;
if (stages.some(name => !all.includes(name))) {
  console.error("Usage: npm run qa -- [ " + all.join(" | ") + " ]...");
  process.exit(2);
}

function run(file, args, cwd, batch = false) {
  return new Promise((resolve, reject) => {
    const child = batch
      ? spawn("cmd.exe", ["/d", "/s", "/c", `""${file}" ${args.join(" ")}"`],
          { cwd, stdio: "inherit", windowsVerbatimArguments: true })
      : spawn(file, args, { cwd, stdio: "inherit" });
    child.once("error", reject);
    child.once("exit", (code, signal) => code === 0 ? resolve() :
      reject(new Error(`${file} exited with ${code ?? signal}`)));
  });
}

function gradleRun(...args) {
  if (windows && /["%\r\n]/.test(gradle)) throw new Error("Invalid Gradle batch path");
  return run(gradle, ["--no-daemon", ...args], android, windows);
}

const tasks = {
  syntax: async () => {
    const files = ["app/app.js", "app/data.js", "app/details.js", "app/trending.js",
      "app/theme.js", "server.cjs", "backend/apiServer.cjs", "backend/eventRepository.cjs", "backend/liveCatalog.cjs", "backend/communityCatalog.cjs"];
    for (const file of files) await run(process.execPath, ["--check", file], root);
  },
  integration: () => run(process.execPath, ["--test", "backend/backend.test.cjs", "backend/communityCatalog.test.cjs"], root),
  "api-e2e": () => run(process.execPath, ["--test", "qa/api.e2e.test.mjs"], root),
  "android-unit": () => gradleRun(":app:testDebugUnitTest", ":app:assembleDebug"),
  "android-e2e": async () => {
    const api = await startApi({ port: 3000 });
    try { await gradleRun(":app:connectedDebugAndroidTest"); }
    finally { await api.stop(); }
  },
};

const results = [];
for (const name of stages) {
  console.log(`\n=== Panacea QA: ${name} ===`);
  const start = Date.now();
  let error;
  try { await tasks[name](); }
  catch (e) { error = e.message; console.error(error); }
  results.push({ stage: name, status: error ? "failed" : "passed", durationMs: Date.now() - start, ...(error && { error }) });
}

const reportDir = join(root, "qa-results");
await mkdir(reportDir, { recursive: true });
await writeFile(join(reportDir, "summary.json"), JSON.stringify({ stages: results }, null, 2) + "\n");
const summary = [
  "## Panacea QA",
  "",
  "| Stage | Result | Seconds |",
  "| --- | --- | ---: |",
  ...results.map(r => `| ${r.stage} | ${r.status} | ${(r.durationMs / 1000).toFixed(1)} |`),
  "",
].join("\n");
console.log(summary);
if (process.env.GITHUB_STEP_SUMMARY) await appendFile(process.env.GITHUB_STEP_SUMMARY, summary);
process.exitCode = results.some(r => r.status === "failed") ? 1 : 0;
