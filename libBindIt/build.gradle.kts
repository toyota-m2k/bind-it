@file:Suppress("LocalVariableName")

plugins {
    id("com.android.library")
    id( "kotlin-android")
    id("com.github.dcendents.android-maven")
}

group = "com.github.toyota-m2k"

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.2")

    defaultConfig {
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode(1)
        versionName("1.0")

        testInstrumentationRunner("androidx.test.runner.AndroidJUnitRunner")
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            minifyEnabled(false)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    val kotlin_version:String by project
    val constraint_layout_version:String by project
    val recycle_view_version:String by project
    val junit_version:String by project
    val material_version:String by project
    val lifecycle_ktx_version:String by project
    val core_ktx_version:String by project
    val appcompat_version:String by project
    val android_junit_version:String by project
    val espresso_version:String by project
    val rx_java_version:String by project
    val rx_android_version:String by project
    val rx_kotlin_version:String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.appcompat:appcompat:$appcompat_version")
    implementation("com.google.android.material:material:$material_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_ktx_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_ktx_version")
    implementation("androidx.constraintlayout:constraintlayout:$constraint_layout_version")
    implementation("androidx.recyclerview:recyclerview:$recycle_view_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_ktx_version")
    implementation("io.reactivex.rxjava3:rxjava:$rx_java_version")
    implementation("io.reactivex.rxjava3:rxandroid:$rx_android_version")
    implementation("io.reactivex.rxjava3:rxkotlin:$rx_kotlin_version")

    testImplementation("junit:junit:$junit_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.1.0")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.mockito:mockito-inline:3.7.7")
    testImplementation("org.robolectric:robolectric:4.3")
    testImplementation ("androidx.arch.core:core-testing:2.1.0")

    androidTestImplementation ("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test.ext:junit:$android_junit_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
}

repositories {
    mavenCentral()
}