import org.ajoberstar.grgit.Grgit

plugins {
    java
    `maven-publish`
    alias(libs.plugins.lavalink)
    alias(libs.plugins.grgit)
}

val (gitVersion, release) = versionFromGit()
logger.lifecycle("Version: $gitVersion (release: $release)")

group = "me.duncte123"
version = gitVersion

lavalinkPlugin {
    name = "java-lyrics-plugin"
    path = "$group.lyrics.lavalink"
    configurePublishing = false
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
}

val isLavalinkMavenDefined = System.getenv("LAVALINK_MAVEN_USERNAME") != null && System.getenv("LAVALINK_MAVEN_PASSWORD") != null

val lavalinkMavenUrl: String
    get() {
        if (release) {
            return "https://maven.lavalink.dev/releases"
        }

        return "https://maven.lavalink.dev/snapshots"
    }

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "org.ajoberstar.grgit")

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    publishing {
        repositories {
            if (isLavalinkMavenDefined && name == "lavalyrics") {
                maven {
                    name = "lavalink"
                    url = uri(lavalinkMavenUrl)
                    credentials {
                        username = System.getenv("LAVALINK_MAVEN_USERNAME")
                        password = System.getenv("LAVALINK_MAVEN_PASSWORD")
                    }
                }
            }
        }
    }
}

allprojects {
    repositories {
        mavenCentral()

        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")
        maven("https://maven.topi.wtf/releases")
        maven("https://maven.topi.wtf/snapshots")
    }
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
    compileOnly(libs.jackson.databind)
    compileOnly(libs.jackson.annotations)
    compileOnly(libs.lavalink.server)
    compileOnly(libs.lavaplayer)

    implementation(projects.protocol)
    implementation(projects.application)
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
    repositories {
        if (isLavalinkMavenDefined) {
            maven {
                name = "lavalink"
                url = uri(lavalinkMavenUrl)
                credentials {
                    username = System.getenv("LAVALINK_MAVEN_USERNAME")
                    password = System.getenv("LAVALINK_MAVEN_PASSWORD")
                }
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "me.duncte123"
            artifactId = "java-lyrics-plugin"
            from(components["java"])
        }
    }
}

fun versionFromGit(): Pair<String, Boolean> {
    Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
        val headTag = git.tag
            .list()
            .find { it.commit.id == git.head().id }

        val clean = git.status().isClean || System.getenv("CI") != null
        if (!clean) {
            logger.lifecycle("Git state is dirty, version is a snapshot.")
        }

        return if (headTag != null && clean) headTag.name to true else "${git.head().id}-SNAPSHOT" to false
    }
}
