plugins {
    java
    `maven-publish`
}

group = "me.duncte123"
version = rootProject.version

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
