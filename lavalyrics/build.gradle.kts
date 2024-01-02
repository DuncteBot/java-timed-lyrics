plugins {
    java
    `maven-publish`
    alias(libs.plugins.lavalink)
}

group = "me.duncte123"
version = rootProject.version

lavalinkPlugin {
    name = "java-lavalyrics"
    path = "$group.lyrics.lavalink"
    configurePublishing = false
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}

dependencies {
    // LL holds all our versions
    compileOnly(libs.lavalink.api)

    compileOnly(libs.lavalyrics)

    implementation(libs.lavalyrics.api)
    implementation(projects.protocol)
    implementation(projects.application)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.duncte123.java-lyrics-plugin"
            artifactId = "lavalyrics"
            from(components["java"])
        }
    }
}
