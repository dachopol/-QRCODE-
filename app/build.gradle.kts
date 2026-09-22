plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

val releaseKeystorePath = System.getenv("KEYSTORE_PATH")
val releaseStorePassword = System.getenv("STORE_PASSWORD")
val releaseKeyPassword = System.getenv("KEY_PASSWORD")
val releaseKeyAlias = System.getenv("KEY_ALIAS") ?: "upload"
val hasReleaseSigning =
  !releaseKeystorePath.isNullOrBlank() &&
  !releaseStorePassword.isNullOrBlank() &&
  !releaseKeyPassword.isNullOrBlank() &&
  file(releaseKeystorePath).exists()

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.qrgenerator.kmpzqr"
    minSdk = 24
    targetSdk = 36
    versionCode = 13
    versionName = "13.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    if (hasReleaseSigning) {
      create("release") {
        storeFile = file(releaseKeystorePath!!)
        storePassword = releaseStorePassword
        keyAlias = releaseKeyAlias
        keyPassword = releaseKeyPassword
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      if (hasReleaseSigning) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    debug {}
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.zxing.core)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)

  "ksp"(libs.androidx.room.compiler)

  testImplementation(libs.junit)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)

  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
}
