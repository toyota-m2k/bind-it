plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.2")

    defaultConfig {
        applicationId = "com.michael.bindit"
        minSdkVersion(26)
        targetSdkVersion(30)
        versionCode(1)
        versionName("1.0")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            setMinifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    val kotlin_version:String by project
    val core_ktx_version:String by project
    val appcompat_version:String by project
    val material_version:String by project
    val lifecycle_ktx_version:String by project
    val constraint_layout_version:String by project

    val junit_version:String by project
    val android_junit_version:String by project
    val espresso_version:String by project

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("androidx.core:core-ktx:$core_ktx_version")
    implementation("androidx.appcompat:appcompat:$appcompat_version")
    implementation("com.google.android.material:material:$material_version")
    implementation("androidx.constraintlayout:constraintlayout:$constraint_layout_version")

    testImplementation("junit:junit:$junit_version")
    androidTestImplementation("org.robolectric:robolectric:4.3")
    androidTestImplementation("androidx.test.ext:junit:$android_junit_version")
    androidTestImplementation("androidx.test.espresso:espresso-core:$espresso_version")
}