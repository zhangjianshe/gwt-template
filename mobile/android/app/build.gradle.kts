plugins {
    id("com.android.application")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

val releaseStore = System.getenv("MOBILE_KEYSTORE_PATH")
val requireReleaseSigning = System.getenv("MOBILE_REQUIRE_SIGNING") == "true"
if (requireReleaseSigning && releaseStore.isNullOrBlank()) {
    error("MOBILE_KEYSTORE_PATH is required for publishing")
}

android {
    namespace = "cn.mapway.cangling_mobile"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "cn.mapway.cangling_mobile"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        // Uses the version code from pubspec.yaml. When using split APKs, 1000 * ABI_VERSION
        // is added automatically by Flutter. (https://developer.android.com/studio/build/configure-apk-splits#configure-APK-versions)
        // You can force using the value of versionCode by specifying the `-P force-version-code-ignoring-abi=true`
        // flag during build.
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    signingConfigs {
        if (!releaseStore.isNullOrBlank()) {
            create("mobileRelease") {
                storeFile = file(releaseStore)
                storePassword = System.getenv("MOBILE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("MOBILE_KEY_ALIAS")
                keyPassword = System.getenv("MOBILE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Local development can use debug signing; CI publication requires a fixed key.
            signingConfig = signingConfigs.getByName(
                if (releaseStore.isNullOrBlank()) "debug" else "mobileRelease"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

flutter {
    source = "../.."
}
