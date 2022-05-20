plugins {
    java
    jacoco
    `maven-publish`
    `java-gradle-plugin`
    id("com.github.spotbugs") version "5.0.6"                   // https://plugins.gradle.org/plugin/com.github.spotbugs
    id("com.diffplug.spotless") version "6.6.1"                 // https://plugins.gradle.org/plugin/com.diffplug.spotless
    id("pl.allegro.tech.build.axion-release") version "1.13.6"  // https://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release
    id("com.github.kt3k.coveralls") version "2.12.0"            // https://plugins.gradle.org/plugin/com.github.kt3k.coveralls
}

project.version = scmVersion.version

apply(plugin = "idea")
apply(plugin = "java")
apply(plugin = "jacoco")
apply(plugin = "checkstyle")
apply(plugin = "com.diffplug.spotless")
apply(plugin = "com.github.spotbugs")
apply(plugin = "maven-publish")

group = "org.creekservice"

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://maven.pkg.github.com/creek-service/*")
        credentials {
            username = "Creek-Bot-Token"
            password = "\u0067hp_LtyvXrQZen3WlKenUhv21Mg6NG38jn0AO2YH"
        }
    }
}

extra.apply {
    set("creekTestVersion", "0.2.0-SNAPSHOT")
    set("creekJsonVersion", "0.2.0-SNAPSHOT")
    set("spotBugsVersion", "4.6.0")         // https://mvnrepository.com/artifact/com.github.spotbugs/spotbugs-annotations

    set("log4jVersion", "2.17.2")           // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    set("guavaVersion", "31.1-jre")         // https://mvnrepository.com/artifact/com.google.guava/guava
    set("junitVersion", "5.8.2")            // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    set("junitPioneerVersion", "1.7.0")     // https://mvnrepository.com/artifact/org.junit-pioneer/junit-pioneer
    set("mockitoVersion", "4.5.1")          // https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
    set("hamcrestVersion", "2.2")           // https://mvnrepository.com/artifact/org.hamcrest/hamcrest-core
}

val creekTestVersion : String by extra
val creekJsonVersion : String by extra
val guavaVersion : String by extra
val log4jVersion : String by extra
val junitVersion: String by extra
val junitPioneerVersion: String by extra
val mockitoVersion: String by extra
val hamcrestVersion : String by extra

dependencies {
    // Avoid non-test dependencies in plugins.

    testImplementation("org.creekservice:creek-test-hamcrest:$creekTestVersion")
    testImplementation("org.creekservice:creek-test-util:$creekTestVersion")
    testImplementation("org.creekservice:creek-test-conformity:$creekTestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.hamcrest:hamcrest-core:$hamcrestVersion")
    testImplementation("com.google.guava:guava-testlib:$guavaVersion")
    testImplementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    testImplementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    // The following dependency is only added to trigger the Github Dependency Bot to update creekSystemTestVersion:
    testRuntimeOnly("org.creekservice:creek-json-schema-generator:$creekJsonVersion")
}

tasks.compileJava {
    options.compilerArgs.add("-Xlint:all,-serial,-requires-automatic,-requires-transitive-automatic")
    options.compilerArgs.add("-Werror")
}

tasks.test {
    useJUnitPlatform()
    setForkEvery(1)
    maxParallelForks = 4
    testLogging {
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
}

gradlePlugin {
    plugins {
        register("CreekPlugin") {
            id = "org.creekservice.json.schema"
            implementationClass = "org.creekservice.api.json.schema.gradle.plugin.JsonSchemaPlugin"
        }
    }
}

tasks.register("writeVersionFile") {
    val outputDir = file("$buildDir/generated/resources/version")
    val versionFile = file("$outputDir/creek-json-schema-generator.version")
    sourceSets.main.get().output.dir(mapOf("buildBy" to "writeVersionFile"), outputDir)

    inputs.property("executorVersion", creekJsonVersion)
    outputs.dir(outputDir).withPropertyName("outputDir")

    doLast {
        outputDir.mkdirs()

        logger.info("Writing creek-system-test-executor version: $creekJsonVersion to $versionFile")
        versionFile.writeText(creekJsonVersion)
    }
}

tasks.processResources {dependsOn(":writeVersionFile")}

spotless {
    java {
        googleJavaFormat("1.12.0").aosp()
        indentWithSpaces()
        importOrder()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

spotbugs {
    tasks.spotbugsMain {
        reports.create("html") {
            required.set(true)
            setStylesheet("fancy-hist.xsl")
        }
    }
    tasks.spotbugsTest {
        reports.create("html") {
            required.set(true)
            setStylesheet("fancy-hist.xsl")
        }
    }
}

tasks.withType<JacocoReport>().configureEach{
    reports {
        xml.required.set(true)
    }

    dependsOn(tasks.test)
}

tasks.register("format") {
    dependsOn("spotlessCheck", "spotlessApply")
}

tasks.register("static") {
    dependsOn("checkstyleMain", "checkstyleTest", "spotbugsMain", "spotbugsTest")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/creek-service/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("pluginMaven") {
            pom {
                url.set("https://github.com/creek-service/${rootProject.name}.git")
            }
        }
    }
}

tasks.register("coverage") {
    group = "coverage"
    dependsOn("jacocoTestReport")
}

tasks.coveralls {
    group = "coverage"
    description = "Uploads the aggregated coverage report to Coveralls"

    dependsOn("coverage")
    onlyIf{System.getenv("CI") != null}
}

defaultTasks("format", "static", "check")
