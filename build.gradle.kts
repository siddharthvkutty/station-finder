plugins { id("com.android.application") version "8.7.3" }

android {
    namespace = "app.stationfinder"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.stationfinder"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    // Commit debug.keystore so every CI build has the same signature and installs as an update.
    signingConfigs.getByName("debug") {
        if (file("debug.keystore").exists()) {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
