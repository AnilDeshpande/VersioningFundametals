import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

fun git(vararg args: String): String? = try {
    val process = ProcessBuilder("git", *args)
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
    val output = process.inputStream.bufferedReader().use { it.readText().trim() }
    if (process.waitFor() == 0) output.takeIf { it.isNotBlank() } else null
} catch (_: Exception) {
    null
}

fun semVerToVersionCode(version: String): Int? {
    val match = Regex("""(\d+)\.(\d+)\.(\d+)""").matchEntire(version) ?: return null
    val (major, minor, patch) = match.destructured
    return major.toInt() * 10000 + minor.toInt() * 100 + patch.toInt()
}

val versionProps = Properties().apply {
    load(rootProject.file("version.properties").inputStream())
}

val major = versionProps["MAJOR"].toString().toInt()
val minor = versionProps["MINOR"].toString().toInt()
val patch = versionProps["PATCH"].toString().toInt()

val fallbackVersionName = "$major.$minor.$patch"
val fallbackVersionCode = major * 10000 + minor * 100 + patch

val latestTag = git("describe", "--tags", "--abbrev=0")
val sha = git("rev-parse", "--short", "HEAD") ?: "nogit"
val commitsSinceTag = latestTag?.let { tag ->
    git("rev-list", "$tag..HEAD", "--count")?.toIntOrNull() ?: 0
} ?: 0

val baseTagVersion = latestTag?.removePrefix("v")
val baseVersionName = baseTagVersion ?: fallbackVersionName
val baseVersionCode = baseTagVersion?.let(::semVerToVersionCode) ?: fallbackVersionCode

// Tagged releases stay clean. Builds ahead of a tag carry trace metadata.
val versionNameFromGit = when {
    latestTag == null -> "$fallbackVersionName+local.$sha"
    commitsSinceTag == 0 -> baseVersionName
    else -> "$baseVersionName+$commitsSinceTag.$sha"
}

val versionCodeFromGit = baseVersionCode + commitsSinceTag

android {
    namespace = "com.codetutor.versioningfundametals"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.codetutor.versioningfundametals"
        minSdk = 24
        targetSdk = 36
        versionCode = versionCodeFromGit
        versionName = versionNameFromGit

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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
