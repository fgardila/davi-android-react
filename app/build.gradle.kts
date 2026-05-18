plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.code93.daviplata"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "dev.code93.daviplata"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("long", "MOCK_DELAY_MS", "1000L")
            buildConfigField("int", "SESSION_TTL_MINUTES", "5")
            buildConfigField("boolean", "DEBUG_TOOLS", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("long", "MOCK_DELAY_MS", "1000L")
            buildConfigField("int", "SESSION_TTL_MINUTES", "15")
            buildConfigField("boolean", "DEBUG_TOOLS", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "META-INF/DEPENDENCIES"
        )
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Seguridad
    implementation(libs.androidx.security.crypto)
    implementation(libs.bcrypt)
    implementation(libs.rootbeer)

    // React Native
    implementation(libs.react.android)
    implementation(libs.hermes.android)

    // Lottie
    implementation(libs.lottie.compose)

    // Test unitario
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)

    // Test instrumentado
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val bundleReactRelease = tasks.register<Exec>("bundleReactRelease") {
    group = "react native"
    description = "Produces index.android.bundle and copies assets for release"
    workingDir = file("${rootDir}/rn-bundle")
    commandLine(
        "npx", "react-native", "bundle",
        "--platform", "android",
        "--dev", "false",
        "--entry-file", "index.js",
        "--bundle-output", "${projectDir}/src/main/assets/index.android.bundle",
        "--assets-dest", "${projectDir}/src/main/res/",
    )
    doFirst {
        file("${projectDir}/src/main/assets").mkdirs()
    }
}

// Wire to Android Gradle's release pre-build after configuration phase
afterEvaluate {
    tasks.findByName("preReleaseBuild")?.dependsOn(bundleReactRelease)
}
