/*
 * Copyright 2023 Creek Contributors (https://github.com/creek-service)
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
    id("pl.allegro.tech.build.axion-release") version "1.15.5" // https://plugins.gradle.org/plugin/pl.allegro.tech.build.axion-release
}

project.version = scmVersion.version

allprojects {
    tasks.jar {
        onlyIf { sourceSets.main.get().allSource.files.isNotEmpty() }
    }
}

val creekVersion = "0.4.2-SNAPSHOT"
val guavaVersion = "32.1.3-jre"               // https://mvnrepository.com/artifact/com.google.guava/guava
val log4jVersion = "2.21.1"                 // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
val junitVersion = "5.10.1"                  // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
val junitPioneerVersion = "2.1.0"           // https://mvnrepository.com/artifact/org.junit-pioneer/junit-pioneer
val mockitoVersion = "5.6.0"                // https://mvnrepository.com/artifact/org.mockito/mockito-junit-jupiter
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
