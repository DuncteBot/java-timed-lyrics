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
    // add your dependencies here
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.duncte123.java-lyrics-plugin"
            artifactId = "protocol"
            from(components["java"])
        }
    }
}
