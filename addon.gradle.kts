tasks.withType<JavaExec>().configureEach {
    if (name.startsWith("runServer")) {
        doFirst("stripClientOnlyMods") {
            // Declared inside the action so the configuration cache can serialize it
            val clientOnlyModIds = listOf("angelica", "ModularUI2")
            classpath = classpath.filter { file ->
                clientOnlyModIds.none { file.name.contains(it, ignoreCase = true) }
            }
        }
    }
}

// Angelica 2.2.x needs RFB's BytePatternMatcher, which only lwjgl3ify 3.x bundles; buildscript 1.0.30 pins 2.1.5
configurations.configureEach {
    if (name.contains("compile", ignoreCase = true)) return@configureEach
    resolutionStrategy.eachDependency {
        if (requested.group == "com.github.GTNewHorizons" && requested.name == "lwjgl3ify") {
            useVersion("3.0.33")
            because("Angelica 2.2.x requires RFB 1.0.8+")
        }
        if (requested.group == "org.lwjgl") {
            useVersion("3.4.2")
            because("lwjgl3ify 3.x is built against LWJGL 3.4")
        }
    }
}

// LWJGL 3.4 modules lwjgl3ify 3.x uses that buildscript 1.0.30 doesn't add
dependencies {
    for (module in listOf("sdl", "spng", "freetype", "harfbuzz", "zstd", "nuklear")) {
        "lwjgl3Classpath"("org.lwjgl:lwjgl-$module:3.4.2")
        for (os in listOf("linux", "linux-arm64", "windows", "macos", "macos-arm64")) {
            "lwjgl3Classpath"("org.lwjgl:lwjgl-$module:3.4.2:natives-$os")
        }
    }
}

// lwjgl3ify 3.x must be entered through RFB's first-thread main
tasks.withType<JavaExec>().configureEach {
    if (name == "runClient17" || name == "runClient21") {
        doFirst("useLwjgl3ify3Bouncer") {
            systemProperty("gradlestart.bouncerClient", "com.gtnewhorizons.retrofuturabootstrap.MainStartOnFirstThread")
            jvmArgs("--enable-native-access=ALL-UNNAMED")
        }
    }
}
