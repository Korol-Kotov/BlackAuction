plugins {
    kotlin("jvm") version "2.3.20-Beta1"
    id("com.gradleup.shadow") version "8.3.0"
}

group = "me.korolkotov"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://maven.leafmc.one/snapshots/")
}

dependencies {
    compileOnly("cn.dreeam.leaf:leaf-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
