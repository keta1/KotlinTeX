plugins {
    `maven-publish`
    signing
}

group = "io.github.mrl"
version = "0.1.0"

publishing {
    repositories {

    }
    publications {
        getByName<MavenPublication>("kotlinMultiplatform") {
            groupId = project.group.toString()
            version = project.version.toString()

            pom {
                name.set("Katex")
                description.set("A multiplatform library for rendering LaTeX math expressions on Android/iOS by Compose Multiplatform")
                url.set("https://github.com/master-lzh/Katex")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    url.set("https://github.com/master-lzh/Katex")
                    connection.set("scm:git:git://github.com/master-lzh/Katex.git")
                    developerConnection.set("scm:git:git://github.com/master-lzh/Katex.git")
                }
            }
        }
    }
}