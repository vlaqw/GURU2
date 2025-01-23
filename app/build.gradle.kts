plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.guru2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.guru2"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    //implementation (files("mysql-connector-java-8.0.26.jar"))
    //implementation("mysql:mysql-connector-java-5.1.49.jar")
    implementation (files("libs/mysql-connector-java-5.1.49.jar"))
    //implementation fileTree(dir: "libs", include: ["mysql-connector-java-5.1.49.jar"])
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")


    //implementation("com.github.mcxiaoke:mysql-connector-android:2.0.1")
    //C:\ProgramData\Microsoft\Windows\Start Menu\Programs\MySQL\MySQL Server 5.7
    //mysql-connector-java-5.1.49.jar
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}