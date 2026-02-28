plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapp"
    buildFeatures {
        viewBinding = true
    }
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- THÊM CÁC THƯ VIỆN NÀY ĐỂ CONFIG API ---
    // Retrofit: Thư viện chính để call API
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Converter Gson: Chuyển dữ liệu JSON từ Server sang Java Object
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    // Glide: Để load ảnh sản phẩm từ URL (Rất quan trọng cho FE)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Thư viện bảo mật của Android Jetpack
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.airbnb.android:lottie:6.0.0")

    // Thư viện xử lý thông báo và chạy ngầm
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.work:work-runtime:2.9.0")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
}