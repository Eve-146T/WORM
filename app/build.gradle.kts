plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "S.N.A.K.E"
    compileSdk = 34

    defaultConfig {
        applicationId = "S.N.A.K.E"
        minSdk = 24
        targetSdk = 34
        versionCode = 1000001
        versionName = "1000001.0-OPTIMIZED"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {


}