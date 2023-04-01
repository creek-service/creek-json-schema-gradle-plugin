/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.json.schema.gradle.plugin.task;

import static org.creekservice.api.json.schema.gradle.plugin.JsonSchemaPlugin.GENERATOR_DEP_ARTEFACT_NAME;
import static org.creekservice.api.json.schema.gradle.plugin.JsonSchemaPlugin.GENERATOR_DEP_GROUP_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.creekservice.api.json.schema.gradle.plugin.JsonSchemaPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

/** Task for generating JSON schemas from code */
public abstract class GenerateJsonSchema extends DefaultTask {

    final ConfigurableFileCollection classPath = getProject().getObjects().fileCollection();

    /** Constructor */
    public GenerateJsonSchema() {

        classPath.from((Callable<Object>) this::getClassFiles);
        classPath.from((Callable<Object>) this::getGeneratorDeps);
        classPath.from((Callable<Object>) this::getProjectDeps);

        setDescription("Generators JSON schemas");
    }

    /**
     * An optional list of module names used to limit the generation of schemas to only those
     * {@code @GeneratesSchema} annotated types within the named modules.
     *
     * <p>Allowed module names can include the glob wildcard {@code *} character.
     *
     * <p>Setting this will speed up schema generation.
     *
     * <p>Default: empty, meaning all modules.
     *
     * @return type scanning module white list property.
     */
    @Input
    public abstract ListProperty<String> getTypeScanningModuleWhiteList();

    /**
     * Method to allow setting allowed subtype packages from the command line.
     *
     * @param args the modules to whitelist
     */
    @SuppressWarnings("unused") // Invoked by Gradle
    @Option(
            option = "type-scanning-allowed-module",
            description =
                    "Restricts the search for @GeneratesSchema annotated types "
                            + "to the supplied allowed module name(s)")
    public void setTypeScanningModuleWhiteListFromOption(final List<String> args) {
        getTypeScanningModuleWhiteList().set(args);
    }

    /**
     * An optional list of package name used to limit the generation of schemas to only those
     * {@code @GeneratesSchema} annotated types under the supplied packages.
     *
     * <p>Allowed package names can include the glob wildcard {@code *} character.
     *
     * <p>Setting this will speed up schema generation.
     *
     * <p>Default: empty, meaning all packages.
     *
     * @return type scanning packaging white list property.
     */
    @Input
    public abstract ListProperty<String> getTypeScanningPackageWhiteList();

    /**
     * Method to allow setting allowed subtype packages from the command line.
     *
     * @param args the packages to whitelist
     */
    @SuppressWarnings("unused") // Invoked by Gradle
    @Option(
            option = "type-scanning-allowed-package",
            description =
                    "Restricts the search for @GeneratesSchema annotated types "
                            + "to under the supplied allowed package name(s)")
    public void setTypeScanningPackageWhiteListFromOption(final List<String> args) {
        getTypeScanningPackageWhiteList().set(args);
    }

    /**
     * An optional list of module name used to limit the search for subtypes to only those types
     * under the supplied module.
     *
     * <p>Allowed module names can include the glob wildcard {@code *} character.
     *
     * <p>Setting this will speed up schema generation.
     *
     * <p>Default: empty, meaning all modules.
     *
     * @return subtype scanning module white list property.
     */
    @Input
    public abstract ListProperty<String> getSubtypeScanningModuleWhiteList();

    /**
     * Method to allow setting allowed subtype modules from the command line.
     *
     * @param args the modules to whitelist
     */
    @SuppressWarnings("unused") // Invoked by Gradle
    @Option(
            option = "subtype-scanning-allowed-module",
            description =
                    "Restricts the search for subtypes, when none are explicitly defined, "
                            + "to under the supplied allowed module name(s)")
    public void setSubtypeScanningModuleWhiteListFromOption(final List<String> args) {
        getSubtypeScanningModuleWhiteList().set(args);
    }

    /**
     * An optional list of package name used to limit the search for subtypes to only those types
     * under the supplied packages.
     *
     * <p>Allowed package names can include the glob wildcard {@code *} character.
     *
     * <p>Setting this will speed up schema generation.
     *
     * <p>Default: empty, meaning all packages.
     *
     * @return subtype scanning package white list property.
     */
    @Input
    public abstract ListProperty<String> getSubtypeScanningPackageWhiteList();

    /**
     * Method to allow setting allowed subtype packages from the command line.
     *
     * @param args the packages to whitelist
     */
    @SuppressWarnings("unused") // Invoked by Gradle
    @Option(
            option = "subtype-scanning-allowed-package",
            description =
                    "Restricts the search for subtypes, when none are explicitly defined, "
                            + "to under the supplied allowed package name(s)")
    public void setSubtypePackageWhiteListFromOption(final List<String> args) {
        getSubtypeScanningPackageWhiteList().set(args);
    }

    /**
     * The resource root where generated schemas should be stored
     *
     * <p>Default: $buildDir/generated/resources/schema
     *
     * @return the resource root property
     */
    @OutputDirectory
    public abstract DirectoryProperty getSchemaResourceRoot();

    /**
     * The name of the directory under the {@link #getSchemaResourceRoot() resource root} where the
     * schema will be written.
     *
     * <p>This corresponds to the directory under which schema files will be located within the
     * compiled jar file.
     *
     * @return output directory property.
     */
    @Input
    public abstract Property<String> getOutputDirectoryName();

    /**
     * @return additional command line arguments to pass to the generator
     */
    @Input
    public abstract ListProperty<String> getExtraArguments();

    /**
     * Method to allow setting extra arguments from the command line.
     *
     * @param args the extra arguments to set.
     */
    @SuppressWarnings("unused") // Invoked by Gradle
    @Option(
            option = "extra-argument",
            description = "Any additional arguments to use when generating schema.")
    public void setExtraArgumentsFromOption(final List<String> args) {
        getExtraArguments().set(args);
    }

    /**
     * @return the class files to scan, e.g. the output of the compileJava task or other compile
     *     task.
     */
    @InputFiles
    public abstract ConfigurableFileCollection getClassFiles();

    /**
     * @return dependencies of the system test runner.
     */
    @Internal
    public abstract ConfigurableFileCollection getGeneratorDeps();

    /**
     * @return dependencies the project needs to compile.
     */
    @Internal
    public abstract ConfigurableFileCollection getProjectDeps();

    /** The task action. */
    @TaskAction
    public void run() {
        checkDependenciesIncludesRunner();

        getProject()
                .javaexec(
                        spec -> {
                            spec.getMainClass()
                                    .set(
                                            "org.creekservice.api.json.schema.generator.JsonSchemaGenerator");
                            spec.getMainModule().set("creek.json.schema.generator");
                            spec.getModularity().getInferModulePath().set(useModulePath());
                            spec.setClasspath(classPath);
                            spec.setArgs(arguments());
                            spec.jvmArgs(jvmArgs());
                        });
    }

    private boolean useModulePath() {
        // If module white lists are provided, run from module-path,
        // Otherwise, from class-path, as some languages, e.g. Groovy, don't play well with
        // module-path yet.
        return !getTypeScanningModuleWhiteList().getOrElse(List.of()).isEmpty()
                || !getSubtypeScanningModuleWhiteList().getOrElse(List.of()).isEmpty();
    }

    private void checkDependenciesIncludesRunner() {
        final Configuration configuration =
                getProject()
                        .getConfigurations()
                        .getByName(JsonSchemaPlugin.GENERATOR_CONFIGURATION_NAME);
        configuration.resolve();

        final Optional<Dependency> executorDep =
                configuration.getDependencies().stream()
                        .filter(dep -> GENERATOR_DEP_GROUP_NAME.equals(dep.getGroup()))
                        .filter(dep -> GENERATOR_DEP_ARTEFACT_NAME.equals(dep.getName()))
                        .findFirst();

        if (executorDep.isEmpty()) {
            throw new MissingExecutorDependencyException();
        }

        getLogger().debug("Using system test executor version: " + executorDep.get().getVersion());
    }

    private List<String> arguments() {
        final List<String> arguments = new ArrayList<>();
        arguments.add(
                "--output-directory="
                        + getSchemaResourceRoot()
                                .getAsFile()
                                .get()
                                .toPath()
                                .resolve(getOutputDirectoryName().get())
                                .toAbsolutePath());

        getTypeScanningModuleWhiteList()
                .get()
                .forEach(name -> arguments.add("--type-scanning-allowed-module=" + name));
        getTypeScanningPackageWhiteList()
                .get()
                .forEach(name -> arguments.add("--type-scanning-allowed-package=" + name));
        getSubtypeScanningModuleWhiteList()
                .get()
                .forEach(name -> arguments.add("--subtype-scanning-allowed-module=" + name));
        getSubtypeScanningPackageWhiteList()
                .get()
                .forEach(name -> arguments.add("--subtype-scanning-allowed-package=" + name));

        arguments.addAll(getExtraArguments().get());
        return arguments;
    }

    private List<String> jvmArgs() {
        final List<String> args = new ArrayList<>();
        if (useModulePath()) {
            args.add("--add-modules=ALL-MODULE-PATH");
        }

        final Object jvmArgs = getProject().findProperty("org.gradle.jvmargs");
        if ((!(jvmArgs instanceof String))) {
            return args;
        }

        args.addAll(List.of(((String) jvmArgs).split("\\s+")));
        return args;
    }

    private static final class MissingExecutorDependencyException extends GradleException {

        MissingExecutorDependencyException() {
            super(
                    "No JSON schema generator dependency found in "
                            + JsonSchemaPlugin.GENERATOR_CONFIGURATION_NAME
                            + " configuration. Please ensure the configuration contains "
                            + GENERATOR_DEP_GROUP_NAME
                            + ":"
                            + GENERATOR_DEP_ARTEFACT_NAME);
        }
    }
}
