import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

version = version("0.1.0")

plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    maven { url = uri("https://jitpack.io/") }
}

dependencies {
    api("commons-io:commons-io:2.7")
    api("com.github.Querz:NBT:6.1")
    compileOnly(project(":common:addons:manifest-addon-loader"))
}

tasks.named<ShadowJar>("shadowJar") {
    relocate("org.apache.commons", "com.dfsek.terra.addons.sponge.lib.commons")
}
