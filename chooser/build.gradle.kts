import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.Properties
import net.thebugmc.gradle.sonatypepublisher.PublishingType.AUTOMATIC

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.3"
}

android {
    namespace = "spa.lyh.cn.chooser"
    compileSdk = 35

    defaultConfig {
        minSdk = 19
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    api(project(":selector"))
    //api("io.github.liyuhaolol:PictureSelector:v3.11.4")
    //implementation("com.google.android.material:material:1.12.0")
}

var signingKeyId = ""//签名的密钥后8位
var signingPassword = ""//签名设置的密码
var secretKeyRingFile = ""//生成的secring.gpg文件目录
var ossrhUsername = ""//sonatype用户名
var ossrhPassword = "" //sonatype密码

val localProperties = project.rootProject.file("local.properties")

if (localProperties.exists()) {
    println("Found secret props file, loading props")
    val properties = Properties()

    InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
        properties.load(reader)
    }
    signingKeyId = properties.getProperty("signingKeyId")
    signingPassword = properties.getProperty("signingPassword")
    secretKeyRingFile = properties.getProperty("secretKeyRingFile")
    ossrhUsername = properties.getProperty("ossrhUsername")
    ossrhPassword = properties.getProperty("ossrhPassword")

} else {
    println("No props file, loading env vars")
}


centralPortal {
    username = ossrhUsername
    password = ossrhPassword
    name = "PictureChooser"
    group = "io.github.liyuhaolol"
    version = "1.0.1"
    pom {
        //packaging = "aar"
        name = "PictureChooser"
        description = "Android PictureSelector Utils"
        url = "https://github.com/liyuhaolol/PictureSelector"
        licenses {
            license {
                name = "Apache License"
                url = "https://github.com/liyuhaolol/PictureSelector/blob/master/LICENSE"
            }
        }
        developers {
            developer {
                id = "liyuhao"
                name = "liyuhao"
                email = "liyuhaoid@sina.com"
            }
        }
        scm {
            connection = "scm:git@github.com/liyuhaolol/PictureSelector.git"
            developerConnection = "scm:git@github.com/liyuhaolol/PictureSelector.git"
            url = "https://github.com/liyuhaolol/PictureSelector"
        }

    }
    publishingType = AUTOMATIC
    javadocJarTask = tasks.create<Jar>("javadocEmptyJar") {
        archiveClassifier = "javadoc"
    }

}


gradle.taskGraph.whenReady {
    if (allTasks.any { it is Sign }) {
        allprojects {
            extra["signing.keyId"] = signingKeyId
            extra["signing.secretKeyRingFile"] = secretKeyRingFile
            extra["signing.password"] = signingPassword
        }
    }
}

signing {
    sign(publishing.publications)
}