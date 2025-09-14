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
        vectorDrawables.useSupportLibrary = true

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
    // Core Android Libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0") // Use the latest one only

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Firebase
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.annotation)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.play.services)

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.13.0")
    implementation(libs.googleid)
    annotationProcessor("com.github.bumptech.glide:compiler:4.13.0")
    implementation("com.squareup.picasso:picasso:2.71828")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Security & Credentials
    implementation("androidx.security:security-crypto:1.0.0")
    implementation(libs.androidx.security.crypto)
    implementation(libs.security.crypto)

    implementation("androidx.credentials:credentials:1.2.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.2.0-alpha02")
    implementation(libs.androidx.credentials)

    // UI Utilities
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Compose (BOM or other dependencies, if applicable)
    implementation(libs.androidx.compose.bom)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(libs.espresso.core) // if used in main code
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")  // Add MPAndroidChart dependency
    implementation ("com.google.android.material:material:1.12.0") // Use the latest stable version

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
