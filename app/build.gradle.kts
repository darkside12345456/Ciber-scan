plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jp.privacyscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jp.privacyscanner"
        minSdk = 26          // Android 8.0 — cobre a esmagadora maioria dos dispositivos
        // targetSdk 34 = Android 14. A Play Store exige um alvo recente para
        // apps novas/atualizações e o mínimo sobe todos os anos — CONFIRMAR o
        // requisito atual e subir (com compileSdk + AGP a condizer) ANTES de
        // submeter. Mantido em 34 aqui para garantir uma primeira build estável.
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Assinatura de release lida de variáveis de ambiente / propriedades Gradle
    // (nunca do código). No CI, guardar numa keystore em base64 nos secrets do
    // repositório e materializá-la antes do assembleRelease. Se não houver
    // keystore, o bloco não é criado e a build de release sai não assinada.
    val keystorePath = System.getenv("KEYSTORE_PATH")
        ?: (project.findProperty("KEYSTORE_PATH") as String?)

    signingConfigs {
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                    ?: (project.findProperty("KEYSTORE_PASSWORD") as String?)
                keyAlias = System.getenv("KEY_ALIAS")
                    ?: (project.findProperty("KEY_ALIAS") as String?)
                keyPassword = System.getenv("KEY_PASSWORD")
                    ?: (project.findProperty("KEY_PASSWORD") as String?)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.security.crypto)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
