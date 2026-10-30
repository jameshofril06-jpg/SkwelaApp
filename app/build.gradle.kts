plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    // Add the serialization plugin (MUST match your Kotlin version)
    kotlin("plugin.serialization") version "1.8.22"
}

android {
    namespace = "com.example.eskwela"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.eskwela"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")
    // Firebase BoM for version management
    implementation(platform("com.google.firebase:firebase-bom:34.8.0"))

    // Firebase Auth & Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}