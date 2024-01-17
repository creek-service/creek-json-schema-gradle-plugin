[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin Portal](https://img.shields.io/maven-central/v/org.creekservice/creek-json-schema-gradle-plugin.svg?label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/org.creekservice.schema.json)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-json-schema-gradle-plugin/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-json-schema-gradle-plugin?branch=main)
[![build](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/build.yml)
[![CodeQL](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-json-schema-gradle-plugin/badge)](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-json-schema-gradle-plugin)
[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/6899/badge)](https://bestpractices.coreinfrastructure.org/projects/6899)

# Creek JSON Schema Gradle Plugin

A Gradle plugin for generating JSON schemas from code using the [Creel JSON Schema Generator][1].

See [CreekService.org](https://www.creekservice.org) for info on Creek Service.

-## Supported Gradle versions

| Gradle Version | Tested version | Notes                                       |
|----------------|----------------|---------------------------------------------|
| < 6.4          |                | Not compatible due to API changes in Gradle |
| 6.4.+          | 6.4            | Supported & tested                          |
| 6.4+           | 6.9.4          | Supported & tested                          |
| 7.+            | 7.6.1          | Supported & tested                          |
| 8.+            | 8.5            | Supported & tested                          |
| > 8.5          |                | Not currently tested. Should work...        |

## Usage

The plugin is available on the [Gradle Plugin Portal][pluginPortal].
See the portal for instructions on how to add the plugin to your build.

## Tasks

The JSON Schema plugin adds the following tasks to your project:

### generateJsonSchema - [GenerateJsonSchema][5]

> ### NOTE
> Details of how to annotate classes to control their schema can be found in the [Creek JSON Schema Generator docs][1].

> ### NOTE
> Restricting class & module path scanning by setting allowed modules and allowed packages can increase the speed of your build. 

*Dependencies:* `compileJava`, `compileKotlin`, `compileGroovy` if they exist.
*Dependants:* `processResources`

The `generateJsonSchema` task searches the class and module path for with [`@GeneratesSchema`][2] and write out
JSON schemas for them in YAML. The generated schema output directory is added to the `main` source set, 
as an additional resource directories, meaning the schema will be included in any generated jar.

Types can be annotated both with [Jackson][3] and [JsonSchema][4] annotations, allowing control of the generated schema.

See the [Creel JSON Schema Generator Docs][1] for more information and examples.

Aside from the customisations possible using the [`creek.schema.json` extension](#json-schema-extension), the task accepts the
following command line options:

* `--type-scanning-allowed-module`: (default: any module) restricts the classes to search to only those belonging to the specified module(s).
  Allowed module names can include the glob wildcard {@code *} character.
* `--type-scanning-allowed-package`: (default: any package) restricts the classes to search to only those under the specified package(s).
  Allowed package names can include the glob wildcard {@code *} character.
* `--subtype-scanning-allowed-module`: (default: any module) restricts the search for subtypes to only those belonging to the specified module(s).
  Allowed module names can include the glob wildcard {@code *} character.
* `--subtype-scanning-allowed-package`: (default: any package) restrict the search for subtypes to only those under the specified package(s).
  Allowed package names can include the glob wildcard {@code *} character.

For example, the following limits the class & module path scanning to only two modules:

```bash
> ./gradlew generateJsonSchema \
    --type-scanning-allowed-module=acme.finance.momdel \
    --type-scanning-allowed-module=acme.sales.model
```

### generateTestJsonSchema - [GenerateJsonSchema][5]

> ### NOTE
> Details of how to annotate classes to control their schema can be found in the [Creek JSON Schema Generator docs][1].

> ### NOTE
> Restricting class & module path scanning by setting allowed modules and allowed packages can increase the speed of your build.

*Dependencies:* `compileTestJava`, `compileTestKotlin`, `compileTestGroovy` if they exist.
*Dependants:* `processTestResources`

The `generateTestJsonSchema` works the same as [generateJsonSchema](#generatejsonschema---generatejsonschema5), only
for test code. The generated schema output directory is added to the `test` source set,
as an additional resource directories, meaning the schema will be available during unit testing.

**NOTE**: due to a [bug](https://github.com/java9-modularity/gradle-modules-plugin/issues/227) in the `org.javamodularity.moduleplugin` Gradle plugin, 
generated resources are NOT patched in to the module during unit testing. 
This should be fixed once https://github.com/java9-modularity/gradle-modules-plugin/pull/228 is merged and released.

### clean*TaskName* - `Delete`

Deletes the files created by the specified task. For example, `cleanGenerateJsonSchema` will delete the generated JSON schema files.

## Project Layout

The JSON Schema plugin does not require any specific project layout. 

## Dependency Management

The JSON Schema plugin adds a number of [dependency configurations][6] to your project.  Tasks such as `generateJsonSchema`
then use these configurations to get the corresponding files and use them, for example by adding them to the class path
when generating schemas.

* `jsonSchemaGenerator` the [JSON schema generator][7] dependency, defaulting to the same version as the plugin.

### Changing the JSON schema generator version

By default, the plugin generates JSON schema using the [JSON schema generator][7] of the same version. However,
you can configure the generator version via the `jsonSchemaGenerator` dependency configuration.

For example, the following would always use the latest available generator:

##### Groovy: Custom JSON Schema executor version
```groovy
dependencies {
    jsonSchemaGenerator 'org.creekservice:creek-json-schema-generator:+'
}
```

##### Kotlin: Custom JSON Schema executor version
```kotlin
dependencies {
    jsonSchemaGenerator("org.creekservice:creek-json-schema-generator:+")
}
```

When running a different version of the generator it may be that the generator supports command line options that
are not exposed by the plugin. In such situations, you can pass extra arguments to the generator using the
`extraArguments` method of the `creek.schema.json` extension.

##### Groovy: Passing extra arguments to the generator
```groovy
creek {
    schema {
        json {
            extraArguments "--some", "--extra=arguments"
        }
    }
}
```

##### Kotlin: Passing extra arguments to the generator
```kotlin
jsonSchema {
    schema {
        json {
            extraArguments("--some", "--extra=arguments")    
        }
    }
}
```

## JSON Schema Extension

The JSON Schema plugin adds a `json` extension to `creek.schema`. This allows you to configure a number of task related properties
inside a dedicated DSL block.

##### Groovy: Using the `creek.schema.json` extension
```groovy
creek.schema.json {
    
    // Configure scanning for @GeneratesSchema annotated types:
    typeScanning {
        // Restrict scanning to certain JPMS modules:
        moduleWhiteList 'acme-finance', 'acme-sales-*'

        // Restrict scanning to certain packages:
        packageWhiteList 'com.acme.finance', 'com.acme.sales.*'
    }

    // Configure scanning subtypes:
    subTypeScanning {
        // Restrict scanning to certain JPMS modules:
        moduleWhiteList 'acme-finance', 'acme-sales-*'

        // Restrict class scanning for subtypes to certain packages:
        packageWhiteList 'com.acme.finance', 'com.acme.sales.*'
    }
}
```

##### Kotlin: Using the `creek.schema.json` extension
```kotlin
creek.schema.json {
    // Configure scanning for @GeneratesSchema annotated types:
    typeScanning {
        // Restrict class scanning to certain JPMS modules:
        moduleWhiteList("acme-finance", "acme-sales-")

        // Restrict class scanning for base types to certain packages:
        packageWhiteList("com.acme.finance", "com.acme.sales.*")
    }

    // Configure scanning subtypes:
    subTypeScanning {
        // Restrict class scanning to certain JPMS modules:
        moduleWhiteList("acme-finance", "acme-sales-")

        // Restrict class scanning for subtypes to certain packages:
        packageWhiteList("com.acme.finance", "com.acme.sales.*")
    }
}
```

## Speeding up class scanning

The [generator][1] scans the class and module paths of the JVM to find:

* **base types** to generate a schema for, i.e. types annotated with [`@GeneratesSchema`][2].
* **subtypes** of polymorphic types it encounters while building schema, where the base type does not define an explicit
  set of subtypes, i.e. types annotated with `@JsonTypeInfo`, but not `@JsonSubTypes`.

By default, these scans scan the entire class and module paths of the JVM. This can both slow down your build and result
in unwanted schema files being generated for annotated types found in dependencies.

The path scans can be restricted to exclude unwanted types from schema generation and speed up the build. 

### Controlling which types generate schemas

Base type scanning, i.e. scanning for types annotated with `@GeneratesSchema`, can be restricted by either supplying
one or more JPMS modules to scan:

##### Groovy: Restricting modules to scan for @GeneratesSchema annotated types
```groovy
creek.schema.json {
    typeScanning {
        moduleWhiteList 'acme-finance', 'acme-sales-*'
    }
}
```

##### Kotlin: Restricting modules to scan for @GeneratesSchema annotated types
```kotlin
creek.schema.json {
    typeScanning {
        moduleWhiteList("acme-finance", "acme-sales")
    }
}
```

...and/or by restricting which package names to search under:

##### Groovy: Restricting packages to scan for @GeneratesSchema annotated types
```groovy
creek.schema.json {
    typeScanning {
        packageWhiteList 'com.acme.finance', 'com.acme.sales.*'
    }
}
```

##### Kotlin: Restricting packages to scan for @GeneratesSchema annotated types
```kotlin
creek.schema.json {
    typeScanning {
        packageWhiteList("com.acme.finance", "com.acme.sales.*")
    }
}
```

### Controlling which subtypes are included in schemas

Subtype scanning, i.e. scanning for subtypes of polymorphic types, can be restricted by restricting which package names
to search under:

##### Groovy: Restricting modules to scan for subtypes
```groovy
creek.schema.json {
    subTypeScanning {
        moduleWhiteList("acme-finance", "acme-sales")
        packageWhiteList 'com.acme.finance', 'com.acme.sales.*'
    }
}
```

##### Kotlin: Restricting modules to scan for subtypes
```kotlin
creek.schema.json {
    subTypeScanning {
        moduleWhiteList("acme-finance", "acme-sales")
        packageWhiteList("com.acme.finance", "com.acme.sales.*")
    }
}
```

## JSON Schema Generation

The `generateJsonSchema` task generates YAML files containing the JSON schema of each `@GeneratesSchema` annotated type
it encounters. By default, these are written to `$buildDir/generated/resources/schema/main/schema/json`, with
`$buildDir/generated/resources/schema/main` being added as a resource root. This means the schema files generated will 
be included in the jar under a `schema/json` directory.

The `generateTestJsonSchema` task outputs to the `$buildDir/generated/resources/schema/test/schema/json`, with
`$buildDir/generated/resources/schema/test` being added as a resource root.

### Changing schema file location

The location where schema files are written can be changed by changing either:

* `creek.schema.json.schemaResourceRoot`: the resource root for schema, defaulting to `$buildDir/generated/resources/schema/main`, or
* `creek.schema.json.testSchemaResourceRoot`: the resource root for test schema, defaulting to `$buildDir/generated/resources/schema/test`, or
* `creek.schema.json.outputDirectoryName`: the name of the subdirectory under `creek.schema.json.schemaResourceRoot` and
  `creek.schema.json.testSchemaResourceRoot` where schemas will be written, 
  and defining the relative path to the schema files within the resulting jar file.

##### Groovy: Customising schema file output location
```groovy
creek.schema.json {
  schemaResourceRoot = file("$buildDir/custom/build/path")
  testSchemaResourceRoot = file("$buildDir/custom/build/path/for/test/schema")
  outputDirectoryName = "custom/path/within/jar"
}
```

##### Kotlin: Customising schema file output location
```kotlin
creek.schema.json {
    schemaResourceRoot.set(file("$buildDir/custom/build/path"))
    testSchemaResourceRoot.set(file("$buildDir/custom/build/path/for/test/schema"))
    outputDirectoryName.set("custom/path/within/jar")
}
```

## JVM Language support

Currently, the plugin automatically configures tasks to work with the standard Java, Groovy and Kotlin plugins. 
Schema generation tasks are configured to read the output of `compile[Test]Java`, `compile[Test]Groovy` and `compile[Test]Kotlin`
tasks if they are present in the project.

Support for other JVM languages may be added later, or you may be able to configure your own instance of `GenerateJsonSchema`
to work with your chosen language. (Consider adding details to [Issue 6][8] if you do).

[1]: https://github.com/creek-service/creek-json-schema/tree/main/generator
[2]: https://github.com/creek-service/creek-base/blob/main/annotation/src/main/java/org/creekservice/api/base/annotation/schema/GeneratesSchema.java
[3]: https://github.com/FasterXML/jackson-annotations
[4]: https://github.com/mbknor/mbknor-jackson-jsonSchema
[5]: src/main/java/org/creekservice/api/json/schema/gradle/plugin/task/GenerateJsonSchema.java
[6]: https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:what-are-dependency-configurations
[7]: https://github.com/creek-service/creek-json-schema/tree/main/generator
[8]: https://github.com/creek-service/creek-json-schema-gradle-plugin/issues/6
[pluginPortal]: https://plugins.gradle.org/plugin/org.creekservice.schema.json
