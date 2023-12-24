plugins {
    java
    `maven-publish`
    alias(libs.plugins.lavalink)
}

group = "me.duncte123"
version = "0.1.0"

lavalinkPlugin {
    name = "java-lyrics"
    path = "$group.lyrics.lavalink"
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
    compileOnly(libs.http)
    compileOnly(libs.jackson)
    compileOnly(libs.lavalink.server)
    compileOnly(libs.lavaplayer)

    implementation(projects.protocol)
}

tasks.wrapper {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.BIN
}

publishing {
    publications {
        /*create<MavenPublication>("maven") {
            groupId = "me.duncte123"
            artifactId = "java-lyrics"
            from(components["java"])
        }*/
    }
}
