import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "eu.florian_fuhrmann"
version = "1.0.1"

repositories {
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://www.jetbrains.com/intellij-repository/releases")
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    // kotlinx-coroutines-core https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // kotlin-reflect https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")

    // from: https://github.com/JetBrains/jewel?tab=readme-ov-file#getting-started
    // See https://github.com/JetBrains/Jewel/releases for the release notes
    implementation("org.jetbrains.jewel:jewel-int-ui-standalone-243:0.27.0")
    implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window-243:0.27.0")
    // Do not bring in Material (we use Jewel)
    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
    }
    // from: https://github.com/JetBrains/jewel?tab=readme-ov-file#icons
    implementation("com.jetbrains.intellij.platform:icons:243.26053.20")

    // https://mvnrepository.com/artifact/org.jetbrains.compose.components/components-splitpane-desktop
    implementation("org.jetbrains.compose.components:components-splitpane-desktop:1.7.3")

    // Reorderable (https://github.com/Calvin-LL/Reorderable/)
    implementation("sh.calvin.reorderable:reorderable:2.4.3")

    // Compose Multiplatform File Picker (https://github.com/Wavesonics/compose-multiplatform-file-picker)
    implementation("com.darkrockstudios:mpfilepicker:3.1.0")

    // Gson https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    // compose-color-picker (https://github.com/godaddy/compose-color-picker)
    implementation("com.godaddy.android.colorpicker:compose-color-picker:0.7.0")
    implementation("com.godaddy.android.colorpicker:compose-color-picker-jvm:0.7.0")
}

compose.desktop {
    application {
        jvmArgs("-Dsun.java2d.uiScale=2.0")
        mainClass = "eu.florian_fuhrmann.musictimedtriggers.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MusicTimedTriggers"
            packageVersion = project.version.toString()
        }
    }
}
