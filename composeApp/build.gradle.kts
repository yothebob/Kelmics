import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
//        macosArm64(),
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
//        maven("https://jogamp.org/deployment/maven")
        google()
    }

    jvm("desktop")
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val desktopMain by getting
        val okioVersion = "3.10.2"
        val platform = "linux"

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
        }
        commonMain.dependencies {
//            implementation("io.github.kevinnzou:compose-webview-multiplatform:2.0.3")
//            implementation("io.ktor:ktor-network:2.3.8")
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(projects.shared)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.arrow.core)
            implementation(libs.arrow.fx.coroutines)
            implementation("com.squareup.okio:okio:$okioVersion")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            val javafxVersion = "23"
            implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
            implementation("org.openjfx:javafx-swing:$javafxVersion:$platform")
        }
        wasmWasiMain.dependencies {
            implementation("io.ktor:ktor-client-websockets-wasm-js:3.1.2")
        }
    }
}

android {
    namespace = "org.kel.mics"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.kel.mics"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.kel.mics.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            linux {
                iconFile.set(project.file("src/androidMain/ic_launcher-playstore.png"))
            }
            packageName = "kelmics"
            packageVersion = "1.0.0"
        }
    }
}
