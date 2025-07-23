plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.wltr.linkycow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wltr.linkycow"
        minSdk = 35
        targetSdk = 36
        versionCode = 5
        versionName = "5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    ndkVersion = "29.0.13599879 rc2"
    buildToolsVersion = "36.0.0"
}

val lifecycle_version = "2.8.3"
val activity_version = "1.9.0"
val ktor_version = "2.3.12"
val datastore_version = "1.1.1"

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    // Annotation processor
    kapt("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
    // activity-compose
    implementation("androidx.activity:activity-compose:$activity_version")
    // navigation-compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Ktor for networking
    implementation("io.ktor:ktor-client-core:2.3.5")
    implementation("io.ktor:ktor-client-okhttp:2.3.5")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
    implementation("io.ktor:ktor-client-logging:2.3.5")
    implementation("org.slf4j:slf4j-nop:1.7.32")

    // Jetpack DataStore
    implementation("androidx.datastore:datastore-preferences:$datastore_version")

    // Icons
    implementation("androidx.compose.material:material-icons-extended-android:1.6.8")

    // LiveData for observing state from SavedStateHandle
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")
}