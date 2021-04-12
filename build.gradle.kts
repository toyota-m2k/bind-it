// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version by extra("1.4.30")
    val recycle_view_version by extra("1.1.0")
    val constraint_layout_version by extra("2.0.4")
    val core_ktx_version by extra("1.3.2")
    val appcompat_version by extra("1.2.0")
    val material_version by extra("1.3.0")
    val lifecycle_ktx_version by extra("2.2.0")
    val rx_java_version by extra("3.0.7")
    val rx_android_version by extra("3.0.0")
    val rx_kotlin_version by extra("3.0.1")

    val junit_version by extra("4.13.1")
    val android_junit_version by extra("1.1.2")
    val espresso_version by extra("3.3.0")
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.github.dcendents:android-maven-gradle-plugin:2.0")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}