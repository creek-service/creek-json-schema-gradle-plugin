pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()

        maven {
            url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        }
    }
}