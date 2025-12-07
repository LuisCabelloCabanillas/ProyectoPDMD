plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.proyectopmdm"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.proyectopmdm"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0-alpha01")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.firebase.database)
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    implementation (platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
// Firestore
    implementation ("com.google.firebase:firebase-firestore-ktx")

// Storage
    implementation ("com.google.firebase:firebase-storage-ktx")


}

private fun DependencyHandlerScope.kapt(string: String) {}
