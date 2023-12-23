plugins {
    java
    `maven-publish`
}

group = "me.duncte123"
version = "0.1.0"

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
        /*create<MavenPublication>("maven") {
            groupId = "me.duncte123"
            artifactId = "java-lyrics"
            from(components["java"])
        }*/
    }
}
