/*
 * Copyright 2025-2026 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    java
    jacoco
    `creek-common-convention`
    `creek-coverage-convention`
    `creek-plugin-publishing-convention`
    `creek-sonatype-publishing-convention`
    id("com.gradle.plugin-publish")
    id("pl.allegro.tech.build.axion-release") version "1.21.3" // https://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release
}

scmVersion {
    versionCreator("simple")
}

project.version = scmVersion.version
println("creekVersion: ${project.version}")

allprojects {
    tasks.jar {
        onlyIf { sourceSets.main.get().allSource.files.isNotEmpty() }
    }
}

val creekVersion = project.version
val guavaVersion = project.property("guavaVersion") as String
val log4jVersion = project.property("log4jVersion") as String
val junitVersion = project.property("junitVersion") as String
val junitPioneerVersion = project.property("junitPioneerVersion") as String
val mockitoVersion = project.property("mockitoVersion") as String

dependencies {
    // Avoid non-test dependencies in plugins.

    testImplementation("org.creekservice:creek-test-hamcrest:$creekVersion")
    testImplementation("org.creekservice:creek-test-util:$creekVersion")
    testImplementation("org.creekservice:creek-test-conformity:$creekVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("com.google.guava:guava-testlib:$guavaVersion")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    // The following dependency is only added to force GitHub Dependency Bot to take the generator version into account
    testRuntimeOnly("org.creekservice:creek-json-schema-generator:$creekVersion")
}

gradlePlugin {
    plugins {
        register("CreekPlugin") {
            id = "org.creekservice.schema.json"
            implementationClass = "org.creekservice.api.json.schema.gradle.plugin.JsonSchemaPlugin"
            displayName = "Creek JSON schema generator plugin"
            description = "Generates JSON schemas from JVM types"
            tags.set(listOf("creek", "creekservice", "json", "schema", "jsonschema", "json-schema", "schema-generator", "generator"))
        }
    }
}

tasks.register("writeVersionFile") {
    val outputDir = layout.buildDirectory.dir("generated/resources/version")
    val versionFile = outputDir.map { dir -> file("$dir/creek-json-schema-generator.version") }
    sourceSets.main.get().output.dir(mapOf("buildBy" to "writeVersionFile"), outputDir)

    inputs.property("executorVersion", creekVersion)
    outputs.dir(outputDir).withPropertyName("outputDir")

    doLast {
        outputDir.get().asFile.mkdirs()

        logger.info("Writing creek-system-test-executor version: $creekVersion to $versionFile")
        versionFile.get().writeText("$creekVersion")
    }
}

tasks.processResources { dependsOn(":writeVersionFile") }

defaultTasks("format", "static", "check")
