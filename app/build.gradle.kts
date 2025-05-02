plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.onlineexamapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.onlineexamapp"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0") // Use only the latest version
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.google.android.material:material:1.11.0")

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // Use only the latest version
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")     // Use only the latest version
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation(libs.androidx.activity.ktx)
    implementation(libs.material)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("androidx.activity:activity-ktx:1.7.2") // For ActivityResultContracts
    implementation ("androidx.appcompat:appcompat:1.6.1") // For app compatibility
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("com.github.bumptech.glide:glide:4.13.0") // Glide version
    implementation ("com.squareup.picasso:picasso:2.71828") // Picasso version (optional)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0") // Glide annotation processor
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.android.material:material:1.12.0") // Or the latest version





}
