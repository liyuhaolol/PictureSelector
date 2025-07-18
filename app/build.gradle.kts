plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.luck.pictureselector"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.luck.pictureselector"
        minSdk = 21
        targetSdk = 35
        versionCode = 93
        versionName = "v3.11.2"
    }

    signingConfigs{
        create("pictureSelectorKey") {
            storeFile = file("signature/picture.jks")//签名文件的path
            storePassword = "luck888A"
            keyAlias = "picture"
            keyPassword = "luck888A"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("pictureSelectorKey")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("pictureSelectorKey")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    //implementation "androidx.activity:activity:1.10.0"
    implementation(project(":compress"))
    implementation(project(":ucrop"))
    //implementation("io.github.liyuhaolol:ucrop:v3.11.3")
    implementation(project(":camerax"))
    implementation(project(":ijkplayer-java"))
    implementation(project(":chooser"))
    //implementation("io.github.liyuhaolol:PictureChooser:1.0.0")
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)
    debugImplementation (libs.leakcanary.android)
    implementation (libs.subsampling.scale.image.view.androidx)
    implementation (libs.androidx.appcompat)
    implementation (libs.exoplayer)
    implementation (libs.androidx.constraintlayout)
    implementation (libs.androidx.recyclerview)
    implementation (libs.androidx.viewpager2)
    implementation (libs.glide)
    implementation (libs.picasso)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)
    implementation (libs.permission)
    implementation (libs.glideutils)
    implementation (libs.io)
}
