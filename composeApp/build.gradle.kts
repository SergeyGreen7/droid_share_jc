import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.regex.Pattern.compile

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

    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.dns.sd.kt)
            implementation(libs.log4j.slf4j2.impl)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)

            implementation(libs.logback.classic)
            implementation(libs.blessed)
            implementation(libs.dbus.java.transport.native.unixsocket)
//            implementation("com.github.weliem.blessed-bluez:blessed:0.64")
            implementation(files("../libs/WinBleNativeApi.jar"))
        }
    }

}

android {
    namespace = "org.example.project"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.example.project"
        minSdk = 28
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
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
        isCoreLibraryDesugaringEnabled = true
    }
    buildToolsVersion = "30.0.0"
    dependenciesInfo {
        includeInBundle = true
    }
    ndkVersion = "28.0.12674087 rc2"
    externalNativeBuild {
        cmake {
            path = file("src/desktopMain/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // implementation(libs.androidx.foundation.layout.android)
    // implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.graphics.android)
    // implementation(libs.androidx.compose.material3)
    // implementation(libs.androidx.compose.material)
    implementation(libs.firebase.inappmessaging.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    // debugImplementation(compose.uiTooling)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}

