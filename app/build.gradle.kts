import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.tennofreunde"
    compileSdk = 36
    val localSigning = Properties().apply {
        rootProject.file(".release-signing/signing.properties").takeIf { it.isFile }?.inputStream()?.use(::load)
    }
    val tennoReleaseStoreFile = localSigning.getProperty("storeFile") ?: providers.gradleProperty("TENNO_RELEASE_STORE_FILE")
        .orElse(providers.environmentVariable("TENNO_RELEASE_STORE_FILE"))
        .orNull

    signingConfigs {
        create("githubRelease") {
            if (!tennoReleaseStoreFile.isNullOrBlank()) {
                storeFile = file(tennoReleaseStoreFile)
                storePassword = localSigning.getProperty("storePassword") ?: providers.gradleProperty("TENNO_RELEASE_STORE_PASSWORD")
                    .orElse(providers.environmentVariable("TENNO_RELEASE_STORE_PASSWORD"))
                    .orNull
                keyAlias = localSigning.getProperty("keyAlias") ?: providers.gradleProperty("TENNO_RELEASE_KEY_ALIAS")
                    .orElse(providers.environmentVariable("TENNO_RELEASE_KEY_ALIAS"))
                    .orNull
                keyPassword = localSigning.getProperty("keyPassword") ?: providers.gradleProperty("TENNO_RELEASE_KEY_PASSWORD")
                    .orElse(providers.environmentVariable("TENNO_RELEASE_KEY_PASSWORD"))
                    .orNull
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.tennofreunde"
        minSdk = 24
        targetSdk = 36
        versionCode = 64
        versionName = "11.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (!tennoReleaseStoreFile.isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("githubRelease")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation("com.google.code.gson:gson:2.10.1")
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
