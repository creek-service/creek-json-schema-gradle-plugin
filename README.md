[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-json-schema-gradle-plugin/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-json-schema-gradle-plugin?branch=main)
[![build](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/gradle.yml/badge.svg)](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/gradle.yml)
[![CodeQL](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-json-schema-gradle-plugin/actions/workflows/codeql.yml)

# Creek JSON Schema Gradle Plugin

A Gradle plugin for generating JSON schemas from code using the [Creel JSON Schema Generator][1].

> ### NOTE
> The plugin works with Gradle 6.4 and above.

## Usage

To use the JSON Schema plugin, include the following in your build script:

##### Groovy: Using the JSON Schema plugin
```groovy
plugins {
    id 'org.creekservice.json.schema'
}
```

##### Kotlin: Using the JSON Schema plugin
```kotlin
plugins {
    id("org.creekservice.json.schema")
}
```

This will add the tasks below, with default configuration.

## Tasks

The JSON Schema plugin adds the following tasks to your project:

### generateJsonSchema - [`GenerateJsonSchema`][5]

> ### NOTE
> Details of how to annotate classes to control their schema can be found in the [Creek JSON Schema Generator docs][1].

> ### NOTE
> Restricting class & module path scanning by setting allowed modules and allowed packages can increase the speed of your build. 

*Dependencies:* `compileJava`, `compileKotlin`, `compileGroovy` if they exist.
*Dependants:* `processResources`

The `generateJsonSchema` task searches the class and module path for with [`@GeneratesSchema`][2] and write out
JSON schemas for them in YAML. The generated schemas are added to the `main` resource set, meaning they will be included
in any generated jar.

Types can be annotated both with [Jackson][3] and [JsonSchema][4] annotations, allowing control of the generated schema.

See the [Creel JSON Schema Generator Docs][1] for more information and examples.

Aside from the customisations possible using the [`jsonSchema` extension](#json-schema-extension), the task accepts the
following command line options:

* `--allowed-module`: (default: any module) restricts the classes to search to only those belonging to the specified module(s).
  Allowed module names can include the glob wildcard {@code *} character.
* `--allowed-base-type-package`: (default: any package) restricts the classes to search to only those under the specified package(s).
  Allowed package names can include the glob wildcard {@code *} character.
* `--allowed-sub-type-package`: (default: any package) restrict the search for subtypes to only those under the specified package(s).
  Allowed package names can include the glob wildcard {@code *} character.

For example, the following limits the class & module path scanning to only two modules:

```bash
> ./gradlew generateJsonSchema \
    --allowed-module=acme.finance.momdel \
    --allowed-module=acme.sales.model
```

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

##### Groovy: Custom JSON Schema executor version
```groovy
dependencies {
    jsonSchemaGenerator 'org.creekservice:creek-json-schema-generator:0.2.0'
}
```

##### Kotlin: Custom JSON Schema executor version
```kotlin
dependencies {
    jsonSchemaGenerator("org.creekservice:creek-json-schema-generator:0.2.0")
}
```

When running a different version of the generator it may be that the generator supports command line options that
are not exposed by the plugin. In such situations, you can pass extra arguments to the generator using the
`extraArguments` method of the `jsonSchema` extension.

##### Groovy: Passing extra arguments to the generator
```groovy
jsonSchema {
    extraArguments "--some", "--extra=arguments"
}
```

##### Kotlin: Passing extra arguments to the generator
```kotlin
jsonSchema {
    extraArguments("--some", "--extra=arguments")
}
```

## JSON Schema Extension

The JSON Schema plugin adds the `jsonSchema` extension. This allows you to configure a number of task related properties
inside a dedicated DSL block.

##### Groovy: Using the `jsonSchema` extension
```groovy
jsonSchema {
    // Restrict class scanning to certain JPMS modules:
    allowedModules 'acme-finance', 'acme-sales-*'
    
    // Restrict class scanning for base types to certain packages:
    allowedBaseTypePackages 'com.acme.finance', 'com.acme.sales.*'

    // Restrict class scanning for subtypes to certain packages:
    allowedSubTypePackages 'com.acme.finance', 'com.acme.sales.*'
}
```

##### Kotlin: Using the `jsonSchema` extension
```kotlin
jsonSchema {
    // Restrict class scanning to certain JPMS modules:
    allowedModules("acme-finance", "acme-sales")

    // Restrict class scanning for base types to certain packages:
    allowedBaseTypePackages("com.acme.finance", "com.acme.sales.*")
  
    // Restrict class scanning for subtypes to certain packages:
    allowedSubTypePackages("com.acme.finance", "com.acme.sales.*")
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

##### Groovy: Restricting modules to scan  for base types
```groovy
jsonSchema {
    allowedModules 'acme-finance', 'acme-sales-*'
}
```

##### Kotlin: Restricting modules to scan for base types
```kotlin
jsonSchema {
    allowedModules("acme-finance", "acme-sales")
}
```

...and/or by restricting which package names to search under:

##### Groovy: Restricting packages to scan for base types
```groovy
jsonSchema {
    allowedBaseTypePackages 'com.acme.finance', 'com.acme.sales.*'
}
```

##### Kotlin: Restricting packages to scan for base types
```kotlin
jsonSchema {
    allowedBaseTypePackages("com.acme.finance", "com.acme.sales.*")
}
```

### Controlling which subtypes are included in schemas

Subtype scanning, i.e. scanning for subtypes of polymorphic types, can be restricted by restricting which package names
to search under:

##### Groovy: Restricting modules to scan
```groovy
jsonSchema {
    allowedSubTypePackages 'com.acme.finance', 'com.acme.sales.*'
}
```

##### Kotlin: Restricting modules to scan
```kotlin
jsonSchema {
    allowedSubTypePackages("com.acme.finance", "com.acme.sales.*")
}
```

## JSON Schema Generation

The `generateJsonSchema` task generates YAML files containing the JSON schema of each `@GeneratesSchema` annotated type
it encounters. By default, these are written to `$buildDir/generated/resources/schema/schema/json`, with
`$buildDir/generated/resources/schema` being added as a resource root. This means the schema files generated will 
be included in the jar under a `schema/json` directory.

### Changing schema file location

The location where schema files are written can be changed by changing either:

* `jsonSchema.schemaResourceRoot`: the resource root for schema, defaulting to `$buildDir/generated/resources/schema`, or
* `jsonSchema.outputDirectoryName`: the name of the subdirectory under `jsonSchema.schemaResourceRoot` where schemas will be written,
   and defining the relative path to the schema files within the resulting jar file.

##### Groovy: Customising schema file output location
```groovy
jsonSchema {
  schemaResourceRoot = file("$buildDir/custom/build/path")
  outputDirectoryName = "custom/path/within/jar"
}
```

##### Kotlin: Customising schema file output location
```kotlin
jsonSchema {
    schemaResourceRoot.set(file("$buildDir/custom/build/path"))
    outputDirectoryName.set("custom/path/within/jar")
}
```

## JVM Language support

Currently, the plugin automatically configures tasks to work with the standard Java, Groovy and Kotlin plugins. 
Schema generation tasks are configured to read the output of `compileJava`, `compileGroovy` and `compileKotlin`
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
