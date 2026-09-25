plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// In GitHub Actions this is "<user>/<repo>", so the APK points at your repo's daily.json.
// For a local build, set GITHUB_REPOSITORY yourself or edit the fallback.
val githubRepo = System.getenv("GITHUB_REPOSITORY") ?: "YOUR_USER/daily-dsa"

android {
    namespace = "com.dailydsa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dailydsa"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "DATA_URL",
            "\"https://raw.githubusercontent.com/$githubRepo/main/data/daily.json\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
}
