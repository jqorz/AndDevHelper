import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.components.resources)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
    targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
}

compose.desktop {
    application {
        mainClass = "com.jqorz.anddevhelper.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AndDevHelper"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("src/main/resources/icons/app.ico"))
                menuGroup = "AndDevHelper"
                shortcut = true
                dirChooser = true
            }

            macOS {
                iconFile.set(project.file("src/main/resources/icons/icon_256x256.png"))
            }

            linux {
                iconFile.set(project.file("src/main/resources/icons/icon_256x256.png"))
            }
        }
    }
}
