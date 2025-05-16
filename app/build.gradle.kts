

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.unipi.george.chordshub"
    compileSdk = 35

    defaultConfig {

        applicationId = "com.unipi.george.chordshub"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    buildFeatures {
        compose = true
    }
}

dependencies {


    implementation ("com.google.accompanist:accompanist-pager:0.34.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.34.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    //kapt ("androidx.room:room-compiler:2.6.1")

    // Για να χρησιμοποιήσεις coroutines με Room
    implementation ("androidx.room:room-ktx:2.6.1")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.31.1-alpha")

    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")
    implementation (libs.coil.compose)
    implementation (libs.cloudinary.android)
    implementation (libs.accompanist.navigation.animation)

    implementation (libs.androidx.material.icons.extended)

    implementation(libs.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}