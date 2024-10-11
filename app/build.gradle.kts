plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.rubberbandimplementation"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.rubberbandimplementation"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "processor"

    productFlavors {
        create("R3Engine") {
            dimension = "processor"
            applicationIdSuffix = ".R3Engine"
            versionNameSuffix = "-rubberband"
            buildConfigField("boolean", "USE_RUBBERBAND", "true")
            buildConfigField("String", "PROCESSOR_TYPE", "\"RubberBand\"")
            resValue("string","app_name","R3Engine")

        }

        create("R2Engine") {
            dimension = "processor"
            applicationIdSuffix = ".R2Engine"
            versionNameSuffix = "-sonic"
            buildConfigField("boolean", "USE_RUBBERBAND", "false")
            buildConfigField("String", "PROCESSOR_TYPE", "\"Sonic\"")
            resValue("string","app_name","R2Engine")
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true  // Enable BuildConfig generation
    }
}

dependencies {
    // Core ExoPlayer library
    implementation("com.google.android.exoplayer:exoplayer-core:2.18.5")
    // UI components for ExoPlayer (if needed)
    implementation("com.google.android.exoplayer:exoplayer-ui:2.18.5")
    // For audio processing and other features
    implementation("com.google.android.exoplayer:exoplayer-dash:2.18.5")
    implementation("com.google.android.exoplayer:exoplayer-hls:2.18.5")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}