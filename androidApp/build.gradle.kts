import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val versionProps = Properties().apply {
    val file = rootProject.file("version.properties")
    if (file.exists()) load(file.inputStream())
}

val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

val vCode = (versionProps["VERSION_CODE"] as? String)?.toInt() ?: 1
val vMajor = versionProps["MAJOR"] ?: "0"
val vMinor = versionProps["MINOR"] ?: "1"
val vPatch = versionProps["PATCH"] ?: "0"
val vName = "$vMajor.$vMinor.$vPatch"

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.aslmmovic.qurancompanion"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.aslmmovic.qurancompanion"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = vCode
        versionName = vName
        resourceConfigurations += listOf("en", "ar")
    }

    signingConfigs {
        create("release") {
            val storeFileName = (keystoreProps["storeFile"] as? String)
                ?: System.getenv("KEYSTORE_FILE_PATH") 
                ?: project.findProperty("KEYSTORE_FILE")?.toString()
            if (storeFileName != null) {
                val resolved = rootProject.file(storeFileName)
                if (resolved.exists()) {
                    storeFile = resolved
                    storePassword = (keystoreProps["storePassword"] as? String)
                        ?: System.getenv("KEYSTORE_PASSWORD") 
                        ?: project.findProperty("KEYSTORE_PASSWORD")?.toString() ?: ""
                    keyAlias = (keystoreProps["keyAlias"] as? String) 
                        ?: System.getenv("KEY_ALIAS") 
                        ?: project.findProperty("KEY_ALIAS")?.toString() ?: ""
                    keyPassword = (keystoreProps["keyPassword"] as? String) 
                        ?: System.getenv("KEY_PASSWORD") 
                        ?: project.findProperty("KEY_PASSWORD")?.toString() ?: ""
                }
            }
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

val storeReleaseDir = layout.projectDirectory.dir("../docs/store/release")

tasks.register<Copy>("exportReleaseBundleToStore") {
    group = "publishing"
    description = "Builds and copies the signed release AAB into docs/store/release/"
    dependsOn("bundleRelease")
    val bundleDir = layout.buildDirectory.dir("outputs/bundle/release")
    from(bundleDir) {
        include("*.aab")
        rename(".*\\.aab", "sahaba-companions-v${vName}-release.aab")
    }
    from(bundleDir) {
        include("*.aab")
        rename(".*\\.aab", "app-release.aab")
    }
    into(storeReleaseDir)
}