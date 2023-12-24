plugins {
    java
    `maven-publish`
    alias(libs.plugins.lavalink)
}

group = "me.duncte123"
version = "1.0.0"

lavalinkPlugin {
    name = "java-lyrics-plugin"
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

tasks.jar {
    archiveBaseName.set("lyrics")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.wrapper {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.BIN
}

publishing {
    publications {
        create<MavenPublication>("lavalink") {
            groupId = "me.duncte123"
            artifactId = "java-lyrics-plugin"
            from(components["java"])
        }
    }
}
