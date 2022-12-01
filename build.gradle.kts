plugins {
    java
    jacoco
    `creek-common-convention`
    `creek-coverage-convention`
    `creek-plugin-publishing-convention`
    `creek-sonatype-publishing-convention`
    id("com.gradle.plugin-publish")
    id("pl.allegro.tech.build.axion-release") version "1.14.3" // https://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release
}

project.version = scmVersion.version

val creekVersion = "0.2.1-SNAPSHOT"
val guavaVersion = "31.1-jre"               // https://mvnrepository.com/artifact/com.google.guava/guava
val log4jVersion = "2.19.0"                 // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
val junitVersion = "5.9.1"                  // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
val junitPioneerVersion = "1.9.1"           // https://mvnrepository.com/artifact/org.junit-pioneer/junit-pioneer
val mockitoVersion = "4.9.0"                // https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
val hamcrestVersion = "2.2"                 // https://mvnrepository.com/artifact/org.hamcrest/hamcrest-core

dependencies {
    // Avoid non-test dependencies in plugins.

    testImplementation("org.creekservice:creek-test-hamcrest:$creekVersion")
    testImplementation("org.creekservice:creek-test-util:$creekVersion")
    testImplementation("org.creekservice:creek-test-conformity:$creekVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.hamcrest:hamcrest-core:$hamcrestVersion")
    testImplementation("com.google.guava:guava-testlib:$guavaVersion")
    testImplementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    // The following dependency is only added to force GitHub Dependency Bot to take the generator version into account
    testRuntimeOnly("org.creekservice:creek-json-schema-generator:$creekVersion")
}

pluginBundle {
    tags = tags + listOf("json", "schema", "jsonschema", "json-schema", "schema-generator", "generator")
}

gradlePlugin {
    plugins {
        register("CreekPlugin") {
            id = "org.creekservice.schema.json"
            implementationClass = "org.creekservice.api.json.schema.gradle.plugin.JsonSchemaPlugin"
            displayName = "Creek JSON schema generator plugin"
            description = "Generates JSON schemas from JVM types"
        }
    }
}

tasks.register("writeVersionFile") {
    val outputDir = file("$buildDir/generated/resources/version")
    val versionFile = file("$outputDir/creek-json-schema-generator.version")
    sourceSets.main.get().output.dir(mapOf("buildBy" to "writeVersionFile"), outputDir)

    inputs.property("executorVersion", creekVersion)
    outputs.dir(outputDir).withPropertyName("outputDir")

    doLast {
        outputDir.mkdirs()

        logger.info("Writing creek-system-test-executor version: $creekVersion to $versionFile")
        versionFile.writeText(creekVersion)
    }
}

tasks.processResources { dependsOn(":writeVersionFile") }

defaultTasks("format", "static", "check")
