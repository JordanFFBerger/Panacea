import { spawn } from "node:child_process";
import { once } from "node:events";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL("../", import.meta.url));

export async function startApi({ port = 0 } = {}) {
  const child = spawn(process.execPath, ["backend/apiServer.cjs"], {
    cwd: root, env: { ...process.env, API_PORT: String(port) }, stdio: ["ignore", "pipe", "pipe"],
  });
  let output = "";
  let exited = false;
  const closed = once(child, "close").catch(() => []).then(() => { exited = true; });
  async function stop() {
    if (!exited) child.kill();
    await closed;
  }
  try {
    const baseUrl = await new Promise((resolve, reject) => {
      const timer = setTimeout(() => reject(new Error("API startup timed out: " + output)), 10000);
      child.on("error", error => { clearTimeout(timer); reject(error); });
      child.on("close", code => { clearTimeout(timer); reject(new Error("API exited " + code + ": " + output)); });
      child.stderr.on("data", chunk => { output = (output + chunk).slice(-2000); });
      child.stdout.on("data", chunk => {
        output = (output + chunk).slice(-2000);
        const match = output.match(/Panacea API running at (http:\/\/127\.0\.0\.1:\d+)/);
        if (match) { clearTimeout(timer); resolve(match[1]); }
      });
    });
    return { baseUrl, stop };
  } catch (error) {
    await stop();
    throw error;
  }
}
