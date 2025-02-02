import cn.lalaki.pub.BaseCentralPortalPlusExtension
import org.apache.tools.ant.taskdefs.condition.Os
import java.lang.RuntimeException

plugins {
    id("java")
    id("maven-publish")
    signing
    id("cn.lalaki.central") version("1.2.5")
}

group = "dk.stuart"
version = "1.1.0"

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

val agent: Configuration by configurations.creating

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.assertj:assertj-core:3.11.1")

    agent("dk.stuart:jtestmemprofiler-native-agent:1.1.0") {
        this.artifact {
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                this.classifier = "windows-x86_64-jdk${JavaVersion.current()}"
                this.extension = "dll"
            } else if (Os.isFamily(Os.FAMILY_MAC)) {
                this.classifier = "osx-x86_64-jdk${JavaVersion.current()}"
                this.extension = "dylib"
            } else if (Os.isFamily(Os.FAMILY_UNIX)) {
                this.classifier = "linux-x86_64-jdk${JavaVersion.current()}"
                this.extension = "so"
            } else {
                throw RuntimeException("unsupported operating system")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-agentpath:${agent.singleFile}")
}

publishing {
    publications {
        create<MavenPublication>("jtestmemprofiler") {
            artifactId = rootProject.name
            groupId = "$group"
            version = "${project.version}"

            from(components.named("java").get())

            pom {
                name.set("JTestMemProfiler agent wrapper")
                description.set("Java library for the native JTestMemProfiler agent shared objects")
                url.set("https://github.com/hstuart/jtestmemprofiler-java")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("hstuart")
                        name.set("Henrik Stuart")
                        email.set("github@hstuart.dk")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/hstuart/jtestmemprofiler-java")
                    developerConnection.set("scm:git:ssh://git@github.com/hstuart/jtestmemprofiler-java")
                    url.set("https://github.com/hstuart/jtestmemprofiler-java")
                }
            }
        }
    }

    repositories {
        maven {
            url =  project.layout.buildDirectory.dir("nonexist").get().asFile.toURI()
        }
    }
}

signing {
    sign(publishing.publications)
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSPHRASE"))
}

centralPortalPlus {
    url = project.layout.buildDirectory.dir("nonexist").get().asFile.toURI()
    username = System.getenv("MAVEN_CENTRAL_USERNAME")
    password = System.getenv("MAVEN_CENTRAL_PASSWORD")
    publishingType = BaseCentralPortalPlusExtension.PublishingType.USER_MANAGED
}
