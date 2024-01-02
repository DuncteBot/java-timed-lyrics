plugins {
    java
//    `maven-publish`
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

    // add your dependencies here
    compileOnly(libs.jackson.annotations)

    implementation(projects.protocol)
}

//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            groupId = "me.duncte123.java-lyrics-plugin"
//            artifactId = "protocol"
//            from(components["java"])
//        }
//    }
//}
