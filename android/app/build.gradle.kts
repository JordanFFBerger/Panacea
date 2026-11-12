import java.net.URI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
val configuredApiUrl = providers.gradleProperty("panaceaApiUrl").orNull?.trimEnd('/')
configuredApiUrl?.let {
    val uri = URI(it)
    require(uri.scheme == "https" && uri.host != null && uri.userInfo == null && uri.query == null && uri.fragment == null) {
        "panaceaApiUrl must be an HTTPS base URL"
    }
}
android {
    namespace = "com.panacea.rougeradar"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.panacea.rougeradar"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug { buildConfigField("String", "EVENT_API_URL", "\"${configuredApiUrl ?: "http://10.0.2.2:3000"}\"") }
        release { buildConfigField("String", "EVENT_API_URL", "\"${configuredApiUrl.orEmpty()}\"") }
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
}
