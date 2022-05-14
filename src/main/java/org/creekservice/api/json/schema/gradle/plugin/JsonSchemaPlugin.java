/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.json.schema.gradle.plugin;

import static org.creekservice.api.json.schema.gradle.plugin.GeneratorVersion.defaultGeneratorVersion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.json.schema.gradle.plugin.task.GenerateJsonSchema;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/** Plugin for generating JSON schemas from code */
public final class JsonSchemaPlugin implements Plugin<Project> {

    public static final String CREEK_EXTENSION_NAME = "creek";
    public static final String SCHEMA_EXTENSION_NAME = "schema";
    public static final String JSON_EXTENSION_NAME = "json";
    public static final String GENERATOR_CONFIGURATION_NAME = "jsonSchemaGenerator";
    public static final String GENERATE_SCHEMA_TASK_NAME = "generateJsonSchema";
    public static final String GROUP_NAME = "Creek";
    public static final List<String> ALL_MODULES = List.of();
    public static final List<String> ALL_PACKAGES = List.of();
    public static final String DEFAULT_RESOURCE_ROOT = "generated/resources/schema";
    public static final String DEFAULT_OUTPUT_FOLDER = "schema/json";
    public static final String GENERATOR_DEP_GROUP_NAME = "org.creekservice";
    public static final String GENERATOR_DEP_ARTEFACT_NAME = "creek-json-schema-generator";

    @Override
    public void apply(final Project project) {
        project.getPluginManager().apply(BasePlugin.class);

        final JsonSchemaExtension extension = registerExtension(project);
        registerGenerateSchemaTask(project, extension);
        registerJsonSchemaConfiguration(project);
        project.afterEvaluate(this::afterEvaluate);
    }

    private JsonSchemaExtension registerExtension(final Project project) {
        final ExtensionAware creekExt =
                ensureExtension(project, CREEK_EXTENSION_NAME, CreekSpec.class);
        final ExtensionAware schemaExt =
                ensureExtension(creekExt, SCHEMA_EXTENSION_NAME, SchemaSpec.class);
        final JsonSchemaExtension extension =
                schemaExt.getExtensions().create(JSON_EXTENSION_NAME, JsonSchemaExtension.class);

        extension.getTypeScanning().getModuleWhiteList().convention(ALL_MODULES);
        extension.getTypeScanning().getPackageWhiteListed().convention(ALL_PACKAGES);
        extension.getSubtypeScanning().getModuleWhiteList().convention(ALL_MODULES);
        extension.getSubtypeScanning().getPackageWhiteListed().convention(ALL_PACKAGES);
        extension
                .getSchemaResourceRoot()
                .convention(project.getLayout().getBuildDirectory().dir(DEFAULT_RESOURCE_ROOT));
        extension.getOutputDirectoryName().convention(DEFAULT_OUTPUT_FOLDER);
        extension.getExtraArguments().convention(List.of());
        return extension;
    }

    void registerGenerateSchemaTask(final Project project, final JsonSchemaExtension extension) {
        final GenerateJsonSchema task =
                project.getTasks().create(GENERATE_SCHEMA_TASK_NAME, GenerateJsonSchema.class);

        task.setGroup(GROUP_NAME);
        task.getTypeScanningModuleWhiteList().set(extension.getTypeScanning().getModuleWhiteList());
        task.getTypeScanningPackageWhiteList()
                .set(extension.getTypeScanning().getPackageWhiteListed());
        task.getSubtypeScanningModuleWhiteList()
                .set(extension.getSubtypeScanning().getModuleWhiteList());
        task.getSubtypeScanningPackageWhiteList()
                .set(extension.getSubtypeScanning().getPackageWhiteListed());
        task.getSchemaResourceRoot().set(extension.getSchemaResourceRoot());
        task.getOutputDirectoryName().set(extension.getOutputDirectoryName());
        task.getExtraArguments().set(extension.getExtraArguments());
    }

    void registerJsonSchemaConfiguration(final Project project) {
        final Configuration cfg = project.getConfigurations().create(GENERATOR_CONFIGURATION_NAME);
        cfg.setVisible(false);
        cfg.setTransitive(true);
        cfg.setCanBeConsumed(false);
        cfg.setCanBeResolved(true);
        cfg.setDescription("Dependency for the Creek JSON schema schema generator");

        final String pluginDep =
                GENERATOR_DEP_GROUP_NAME
                        + ":"
                        + GENERATOR_DEP_ARTEFACT_NAME
                        + ":"
                        + defaultGeneratorVersion();
        final DependencyHandler projectDeps = project.getDependencies();
        cfg.defaultDependencies(deps -> deps.add(projectDeps.create(pluginDep)));

        project.getTasks()
                .withType(GenerateJsonSchema.class)
                .configureEach(task -> task.getGeneratorDeps().from(cfg));
    }

    private void afterEvaluate(final Project project) {
        final GenerateJsonSchema generateTask =
                (GenerateJsonSchema) project.getTasks().getByName(GENERATE_SCHEMA_TASK_NAME);

        final SourceSetContainer sourceSetContainer =
                project.getExtensions().findByType(SourceSetContainer.class);
        if (sourceSetContainer != null) {
            final SourceSet mainSourceSet =
                    sourceSetContainer.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            mainSourceSet
                    .getOutput()
                    .dir(
                            Map.of("buildBy", GENERATE_SCHEMA_TASK_NAME),
                            generateTask.getSchemaResourceRoot());
        }

        final Configuration compileClassPathConfig =
                project.getConfigurations()
                        .findByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME);

        if (compileClassPathConfig != null) {
            generateTask.getProjectDeps().from(compileClassPathConfig);
        }

        final Set<Task> compileTasks = compileTasks(project);

        compileTasks.forEach(
                compileTask -> {
                    generateTask.dependsOn(compileTask);
                    generateTask.getClassFiles().from(compileTask.getOutputs());
                });

        generateTask.onlyIf(t -> compileTasks.stream().anyMatch(Task::getDidWork));

        project.getTasksByName(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, false)
                .forEach(processTask -> processTask.dependsOn(generateTask));
    }

    private Set<Task> compileTasks(final Project project) {
        return Stream.of(JavaPlugin.COMPILE_JAVA_TASK_NAME, "compileKotlin", "compileGroovy")
                .map(taskName -> project.getTasksByName(taskName, false))
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    private <T extends ExtensionAware> ExtensionAware ensureExtension(
            final ExtensionAware extensionAware, final String name, final Class<T> type) {
        final ExtensionContainer extensions = extensionAware.getExtensions();
        final Object maybeExt = extensions.findByName(name);
        if (maybeExt != null) {
            return (ExtensionAware) maybeExt;
        }

        return extensions.create(name, type);
    }

    public abstract static class CreekSpec implements ExtensionAware {

        @Override
        public abstract ExtensionContainer getExtensions();
    }

    public abstract static class SchemaSpec implements ExtensionAware {

        @Override
        public abstract ExtensionContainer getExtensions();
    }
}
