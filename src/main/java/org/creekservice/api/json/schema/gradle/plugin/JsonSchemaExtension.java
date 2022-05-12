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


import java.util.List;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public abstract class JsonSchemaExtension {

    /**
     * An optional list of module names used to limit the generation of schemas to only those
     * {@code @GeneratesSchema} annotated types within the named modules.
     *
     * <p>Allowed module names can include the glob wildcard {@code *} character.
     *
     * <p>Setting this will speed up schema generation.
     *
     * <p>Default: empty, meaning all modules.
     */
    public abstract ListProperty<String> getAllowedModules();

    /**
     * Set allowed modules.
     *
     * <p>See {@link #getAllowedModules()} for more info.
     *
     * @param moduleNames the module names to allow.
     */
    @SuppressWarnings("unused") // Invoked from Gradle
    public void allowedModules(final String... moduleNames) {
        getAllowedModules().set(List.of(moduleNames));
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
     */
    public abstract ListProperty<String> getAllowedBaseTypePackages();

    /**
     * Set allowed base type packages.
     *
     * <p>See {@link #getAllowedBaseTypePackages()} for more info.
     *
     * @param packageNames the package names to allow.
     */
    @SuppressWarnings("unused") // Invoked from Gradle
    public void allowedBaseTypePackages(final String... packageNames) {
        getAllowedBaseTypePackages().set(List.of(packageNames));
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
     */
    public abstract ListProperty<String> getAllowedSubTypePackages();

    /**
     * Set allowed sub type packages.
     *
     * <p>See {@link #getAllowedSubTypePackages()} for more info.
     *
     * @param packageNames the package names to allow.
     */
    @SuppressWarnings("unused") // Invoked from Gradle
    public void allowedSubTypePackages(final String... packageNames) {
        getAllowedSubTypePackages().set(List.of(packageNames));
    }

    /**
     * Optional resource root where generated schemas should be stored
     *
     * <p>Default: {@code $buildDir/generated/resources/schema}
     */
    public abstract DirectoryProperty getSchemaResourceRoot();

    /**
     * Optional name of the directory under the {@link #getSchemaResourceRoot() resource root} where
     * the schema will be written.
     *
     * <p>This corresponds to the directory under which schema files will be located within the
     * compiled jar file.
     *
     * <p>Default: {@code schema/json}
     */
    public abstract Property<String> getOutputDirectoryName();

    /**
     * Optional list of additional arguments to pass to the generator.
     *
     * <p>See https://github.com/creek-service/creek-json-schema/tree/main/generator for more info.
     *
     * <p>Default: none.
     */
    public abstract ListProperty<String> getExtraArguments();

    /**
     * Set additional args to pass to the schema generator.
     *
     * <p>See {@link #getExtraArguments()} for more info.
     *
     * @param args the extra args to pass to the generator.
     */
    @SuppressWarnings("unused") // Invoked from Gradle
    public void extraArguments(final String... args) {
        getExtraArguments().set(List.of(args));
    }
}
