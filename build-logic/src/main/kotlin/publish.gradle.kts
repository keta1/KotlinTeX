plugins {
    id("com.vanniktech.maven.publish")
}

group = "io.github.darriousliu"
version = findProperty("version")?.toString().orEmpty()

val commonPom = Action<MavenPom> {
    name.set("KotlinTeX")
    description.set("A multiplatform library for rendering LaTeX math expressions on Android/iOS by Compose Multiplatform")
    url.set("https://github.com/darriousliu/KotlinTeX")

    licenses {
        license {
            name.set("MIT License")
            url.set("https://opensource.org/licenses/MIT")
        }
    }
    developers {
        developer {
            id.set("darriousliu")
            name.set("Darrious Liu")
        }
    }
    scm {
        url.set("https://github.com/darriousliu/KotlinTeX")
        connection.set("scm:git:git://github.com/darriousliu/KotlinTeX.git")
        developerConnection.set("scm:git:git://github.com/darriousliu/KotlinTeX.git")
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/darriousliu/KotlinTeX")
            credentials(PasswordCredentials::class)
        }
    }
    publications {
        getByName<MavenPublication>("kotlinMultiplatform") {
            groupId = project.group.toString()
            version = project.version.toString()

            pom(commonPom)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    pom(commonPom)
}