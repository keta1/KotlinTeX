plugins {
    `maven-publish`
    signing
}

group = "io.github.mrl"
version = "0.1.0"

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/master-lzh/KotlinTeX")
            credentials {
                username = System.getenv("GITHUB_PUBLISH_USERNAME")
                password = System.getenv("GITHUB_PUBLISH_TOKEN")
            }
        }
    }
    publications {
        getByName<MavenPublication>("kotlinMultiplatform") {
            groupId = project.group.toString()
            version = project.version.toString()

            pom {
                name.set("KotlinTeX")
                description.set("A multiplatform library for rendering LaTeX math expressions on Android/iOS by Compose Multiplatform")
                url.set("https://github.com/master-lzh/KotlinTeX")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    url.set("https://github.com/master-lzh/KotlinTeX")
                    connection.set("scm:git:git://github.com/master-lzh/KotlinTeX.git")
                    developerConnection.set("scm:git:git://github.com/master-lzh/KotlinTeX.git")
                }
            }
        }
    }
}