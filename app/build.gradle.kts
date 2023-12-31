plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kittyandpuppy.withallmyanimal"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.kittyandpuppy.withallmyanimal"
        minSdk = 24
        targetSdk = 33
        versionCode = 2
        versionName = "1.02"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.19")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //원형테두리
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    // firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.firebaseui:firebase-ui-storage:8.0.2")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // coil
    implementation("io.coil-kt:coil:2.4.0")
    // 새로고침
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    // 스켈레톤 로딩화면
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
}