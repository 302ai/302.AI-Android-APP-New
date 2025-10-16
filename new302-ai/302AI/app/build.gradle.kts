plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    id("kotlin-parcelize")
}

android {
    namespace = "com.newAi302.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.newAi302.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    //签名打包
    signingConfigs {
        create("keyStore") {
            keyAlias = "proxy"
            keyPassword = "123456"
            storeFile = file("proxy302.jks")
            storePassword = "123456"
        }
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

    buildFeatures {

        viewBinding = true

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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4")
    //popup window
    implementation("com.github.zyyoona7:EasyPopup:1.1.2")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.3")
    // Retrofit 的 Moshi 转换器
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    //implementation("com.squareup.retrofit2:adapter-kotlin-coroutines:2.9.0") // 关键依赖
    //okhttp
    implementation("com.squareup.okhttp3:okhttp:3.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    //rxjava
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.6.3")
    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.0.1")
    //dataStore
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Lifecycle 依赖
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    //kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    //room
    implementation("androidx.room:room-runtime:2.4.3")
    kapt("androidx.room:room-compiler:2.4.0")
    implementation("com.google.code.gson:gson:2.8.0")
    implementation("androidx.room:room-ktx:2.4.3")
    //recycleView item侧滑栏
    implementation("com.github.mcxtzhang:SwipeDelMenuLayout:V1.2.1")

    //动态获取权限
    implementation("pub.devrel:easypermissions:3.0.0")

    //webView数据桥接
    implementation("com.github.lzyzsd:jsbridge:1.0.4")
    // Markwon核心库（最新版本查看https://github.com/noties/Markwon）
    implementation("io.noties.markwon:core:4.6.2")
    // 常用扩展插件
    implementation("io.noties.markwon:html:4.5.0")
    implementation("io.noties.markwon:ext-strikethrough:4.6.0")
    implementation("io.noties.markwon:ext-tasklist:4.6.0")
    implementation("io.noties.markwon:ext-tables:4.6.0")
    implementation("io.noties.markwon:linkify:4.6.2")
    // LaTeX 数学公式渲染插件
    implementation("io.noties.markwon:ext-latex:4.6.2")
    // 图片加载（使用Glide）
    implementation("io.noties.markwon:image-glide:4.6.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")


    // PhotoView 图片缩放库（用于放大预览）
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    //Material库
    implementation("com.google.android.material:material:1.9.0")   // 使用最新版本

//    implementation("com.github.jinguangyang:ChineseTTS:1.0.0")

//    implementation("org.tensorflow:tensorflow-lite-support:0.3.1")
//    implementation("org.tensorflow:tensorflow-lite-select-tf-ops:0.3.1")
    // Kotlin序列化（用于将Map转换为JSON）
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    implementation("com.airbnb.android:lottie:4.2.1")  //这个需要可能会编译不通过

    implementation("com.tencent:mmkv-static:1.3.4")

    //eventbus
    implementation("org.greenrobot:eventbus:3.3.1")

    implementation("com.alibaba:fastjson:1.2.46")

    //地区选择器
    implementation("com.github.joielechong:countrycodepicker:2.4.2")
    //判断手机运营商的库
    implementation("io.michaelrocks:libphonenumber-android:8.13.35")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-analytics")
    // Firebase Cloud Messaging (Java)
    implementation("com.google.firebase:firebase-messaging")
    // Firebase Cloud Messaging (Kotlin)
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-messaging-directboot")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.android.gms:play-services-analytics:18.1.0")
    implementation("com.google.android.gms:play-services-gcm:17.0.0")

    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //switchButton
    implementation("com.kyleduo.switchbutton:library:2.1.0")

    implementation("com.github.lzyzsd:jsbridge:1.0.4")

    implementation("com.google.android.flexbox:flexbox:3.0.0") // 最新版本请确认




}