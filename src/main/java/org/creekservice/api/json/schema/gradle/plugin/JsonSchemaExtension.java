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

package org.creekservice.api.json.schema.gradle.plugin;

import java.util.List;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/** Gradle extension for configuring schema generation */
public abstract class JsonSchemaExtension implements ExtensionAware {

    private final TypeScanningSpec typeScanning;
    private final TypeScanningSpec subTypeScanning;

    /**
     * Gradle extension for configuring which packages and modules are included when scanning for
     * subtypes.
     */
    public abstract static class TypeScanningSpec {

        /**
         * An optional list of module names used to limit type scanning to only the specified
         * modules.
         *
         * <p>Allowed module names can include the glob wildcard {@code *} character.
         *
         * <p>Default: empty, meaning all modules will be scanned.
         *
         * <p><b>NOTE</b>: Setting a non-empty module whitelist also causes the schema generator to
         * be executed from the module-path, rather than the default class-path, i.e. the generator
         * will run under the JPMS. This can run into issues with split packages, i.e. the same
         * package exposed from multiple jars.
         *
         * @return the module whitelist property
         */
        public abstract ListProperty<String> getModuleWhiteList();

        /**
         * Set modules to scan.
         *
         * <p>See {@link #getModuleWhiteList()} for more info.
         *
         * @param moduleNames the module names to allow.
         */
        @SuppressWarnings("unused") // Invoked from Gradle
        public void moduleWhiteList(final String... moduleNames) {
            getModuleWhiteList().set(List.of(moduleNames));
        }

        /**
         * An optional list of package name used to limit type scanning to only the specified
         * packages.
         *
         * <p>Allowed package names can include the glob wildcard {@code *} character.
         *
         * <p>Default: empty, meaning all packages will be scanned.
         *
         * @return the property whitelist property
         */
        public abstract ListProperty<String> getPackageWhiteListed();

        /**
         * Set packages to scan.
         *
         * <p>See {@link #getPackageWhiteListed()} for more info.
         *
         * @param packageNames the package names to allow.
         */
        @SuppressWarnings("unused") // Invoked from Gradle
        public void packageWhiteList(final String... packageNames) {
            getPackageWhiteListed().set(List.of(packageNames));
        }
    }

    /** Constructor */
    public JsonSchemaExtension() {
        this.typeScanning = getExtensions().create("typeScanning", TypeScanningSpec.class);
        this.subTypeScanning = getExtensions().create("subTypeScanning", TypeScanningSpec.class);
    }

    /**
     * Configure type scanning for finding types to generate schema for, i.e. types annotated with
     * {@code GeneratesSchema}.
     *
     * @return the type scanning config
     */
    public TypeScanningSpec getTypeScanning() {
        return typeScanning;
    }

    /**
     * Configure type scanning for finding subtypes, i.e. subtypes of polymorphic types that are
     * part of base types.
     *
     * @return the type scanning config
     */
    public TypeScanningSpec getSubtypeScanning() {
        return subTypeScanning;
    }

    /**
     * Optional resource root where generated schemas should be stored
     *
     * <p>Default: {@code $buildDir/generated/resources/schema}
     *
     * @return the resource root property.
     */
    public abstract DirectoryProperty getSchemaResourceRoot();

    /**
     * Optional resource root where generated test schemas should be stored
     *
     * <p>Default: {@code $buildDir/generated/test-resources/schema}
     *
     * @return the test resource root property.
     */
    public abstract DirectoryProperty getTestSchemaResourceRoot();

    /**
     * Optionally, provide a specific directory to generate schemas into.
     *
     * <p>This is a directory under the {@link #getSchemaResourceRoot() resource root} and {@link
     * #getTestSchemaResourceRoot() test resource root}.
     *
     * <p>By default, schemas will be generated in a package directory structure under the resource
     * root, and the schema filename will match the simple name of the type.
     *
     * <p>If this directory name is set, schemas will be generated into a single flat directly under
     * the resource root, and the schema filename will match the types fully-qualified name.
     *
     * <p>For example, given a type {@code io.acme.financial.model.Account}, the default schema
     * location is {@code resource-root/io/acme/financial/model/Account.yml}. If this output
     * directory property is set, then the schema location is {@code
     * resource-root/output-directory/io.acme.financial.model.Account.yml}.
     *
     * @return output directory property.
     */
    public abstract Property<String> getOutputDirectoryName();

    /**
     * Optional list of additional arguments to pass to the generator.
     *
     * <p>See the <a
     * href="https://github.com/creek-service/creek-json-schema/tree/main/generator">docs</a> for
     * more info.
     *
     * <p>Default: none.
     *
     * @return extra args property.
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
