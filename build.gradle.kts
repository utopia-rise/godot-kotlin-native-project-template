val platform: String by project
val armArch: String by project
val iosSigningIdentity: String by project
val buildType: String? by project

buildscript {
    repositories {
        mavenLocal()
        maven("https://dl.bintray.com/utopia-rise/kotlin-godot")
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
        classpath("org.godotengine.kotlin:godot-gradle-plugin:0.1.0-3.2")
    }
}

repositories {
    mavenLocal()
    maven("https://dl.bintray.com/utopia-rise/kotlin-godot")
    maven(url = "https://dl.bintray.com/utopia-rise/kotlinx")

    //Here we exclude jetbrains coroutines and atomicfu because they do not provide the ones for android platform
    //so we exclude them so that those dependencies are downloaded from our bintray, where we provide android dependencies
    jcenter {
        content {
            excludeModule("org.jetbrains.kotlinx", "kotlinx-coroutines-core-native")
            excludeModule("org.jetbrains.kotlinx", "atomicfu-native")
        }
    }
    mavenCentral {
        content {
            excludeModule("org.jetbrains.kotlinx", "kotlinx-coroutines-core-native")
            excludeModule("org.jetbrains.kotlinx", "atomicfu-native")
        }
    }
}

plugins {
    id("org.jetbrains.kotlin.multiplatform") version ("1.3.61")
}

apply(plugin = "godot-gradle-plugin")

configure<org.godotengine.kotlin.gradleplugin.KotlinGodotPluginExtension> {
    this.releaseType = if (buildType?.toLowerCase() == "release") {
        org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.RELEASE
    } else {
        org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG
    }
    this.godotProjectPath = "${project.rootDir.absolutePath}/.."
    this.libraryPath = "kotlin.gdnlib"
    this.configureTargetAction = ::configureTargetAction
}

kotlin {
    sourceSets {
        sourceSets.create("macosMain")
        sourceSets.create("linuxMain")
        sourceSets.create("windowsMain")
        sourceSets.create("androidArm64Main")
        sourceSets.create("androidX64Main")
        sourceSets.create("iosArm64Main")
        sourceSets.create("iosX64Main")
        configure(listOf(
                sourceSets["macosMain"],
                sourceSets["linuxMain"],
                sourceSets["windowsMain"],
                sourceSets["androidArm64Main"],
                sourceSets["androidX64Main"],
                sourceSets["iosArm64Main"],
                sourceSets["iosX64Main"]
        )) {
            this.kotlin.srcDir("src/main/kotlin")
        }
    }

    if (project.hasProperty("platform")) {
        when (platform) {
            "windows" -> listOf(targetFromPreset(presets["godotMingwX64"], "windows"))
            "linux" -> listOf(targetFromPreset(presets["godotLinuxX64"], "linux"))
            "macos" -> listOf(targetFromPreset(presets["godotMacosX64"], "macos"))
            "android" -> if (project.hasProperty("armArch")) {
                when(armArch) {
                    "X64" -> listOf(targetFromPreset(presets["godotAndroidNativeX64"], "androidX64"))
                    "arm64" -> listOf(targetFromPreset(presets["godotAndroidNativeArm64"], "androidArm64"))
                    else -> listOf(targetFromPreset(presets["godotAndroidNativeArm64"], "androidArm64"))
                }
            } else listOf(targetFromPreset(presets["godotAndroidNativeArm64"], "androidArm64"))
            "ios" -> if (project.hasProperty("armArch")) {
                when (armArch) {
                    "arm64" -> listOf(targetFromPreset(presets["godotIosArm64"], "iosArm64"))
                    "X64" -> listOf(targetFromPreset(presets["godotIosX64"], "iosX64"))
                    else -> listOf(targetFromPreset(presets["godotIosArm64"], "iosArm64"))
                }
            } else listOf(targetFromPreset(presets["godotIosArm64"], "iosArm64"))
            else -> listOf(targetFromPreset(presets["godotLinuxX64"], "linux"))
        }
    } else {
        listOf(
                targetFromPreset(presets["godotLinuxX64"], "linux"),
                targetFromPreset(presets["godotMacosX64"], "macos"),
                targetFromPreset(presets["godotMingwX64"], "windows"),
                targetFromPreset(presets["godotAndroidNativeArm64"], "androidArm64"),
                targetFromPreset(presets["godotAndroidNativeX64"], "androidX64"),
                targetFromPreset(presets["godotIosArm64"], "iosArm64"),
                targetFromPreset(presets["godotIosX64"], "iosX64")
        )
    }
}

fun configureTargetAction(kotlinTarget: @ParameterName(name = "target") org.jetbrains.kotlin.gradle.plugin.KotlinTarget) {
    kotlinTarget.compilations.getByName("main") {
        if (this is org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation) {
            println("Configuring target ${this.target.name}")
            this.target.compilations.all {
                dependencies {
                    implementation("org.godotengine.kotlin:godot-library-extension:0.1.0-3.2")
                    implementation("org.godotengine.kotlin:annotations:0.1.0-3.2")
                }
            }
            if (project.hasProperty("iosSigningIdentity") && this.target.name == "iosArm64") {
                tasks.build {
                    doLast {
                        exec {
                            commandLine = listOf("codesign", "-f", "-s", iosSigningIdentity, "build/bin/iosArm64/releaseShared/libkotlin.dylib")
                        }
                        exec {
                            commandLine = listOf("install_name_tool", "-id", "@executable_path/dylibs/ios/libkotlin.dylib", "build/bin/iosArm64/releaseShared/libkotlin.dylib")
                        }
                    }
                }
            } else if (project.hasProperty("iosSigningIdentity") && this.target.name == "iosX64") {
                tasks.build {
                    doLast {
                        exec {
                            commandLine = listOf("codesign", "-f", "-s", iosSigningIdentity, "build/bin/iosX64/releaseShared/libkotlin.dylib")
                        }
                        exec {
                            commandLine = listOf("install_name_tool", "-id", "@executable_path/dylibs/ios/libkotlin.dylib", "build/bin/iosX64/releaseShared/libkotlin.dylib")
                        }
                    }
                }
            }
        } else {
            System.err.println("Not a native target! TargetName: ${kotlinTarget.name}")
        }
    }
}