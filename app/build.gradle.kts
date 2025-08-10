import com.google.protobuf.gradle.*

plugins {
  idea
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  id("com.google.protobuf") version "0.9.4" // Add this line for the Protobuf plugin
}

android {
  namespace = "com.redpup.racingregisters.companion"
  compileSdk = 35

  sourceSets {
    getByName("main") {
      manifest.srcFile("src/main/AndroidManifest.xml")
      java.srcDirs("src/main/java")
      assets.srcDirs(File("src/main/res"))
      withGroovyBuilder {
        "proto" {
          "srcDir" ("src/main/proto")
        }
      }
    }
  }

  defaultConfig {
    applicationId = "com.redpup.racingregisters.companion"
    minSdk = 29
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables.useSupportLibrary = true
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    compose = true
  }
}

// Corrected protobuf block
protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:3.25.1" // Use a recent stable version
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        create("java") {
          option("lite")
        }
        create("kotlin") {
          option("lite")
        }
      }
    }
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compiler)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.guava)
  implementation(libs.junit)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.test.v1102)
  implementation(libs.protobuf.javalite)
  implementation(libs.protobuf.kotlin.lite)

  testImplementation(libs.jetbrains.kotlinx.coroutines.test)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockk)
  testImplementation(libs.truth)
  testImplementation(libs.turbine)

  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)

  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}