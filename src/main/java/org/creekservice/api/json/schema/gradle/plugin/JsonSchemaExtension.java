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
     * Optional name of the directory under the {@link #getSchemaResourceRoot() resource root} where
     * the schema will be written.
     *
     * <p>This corresponds to the directory under which schema files will be located within the
     * compiled jar file.
     *
     * <p>Default: {@code schema/json}
     *
     * @return output directory name property.
     */
    public abstract Property<String> getOutputDirectoryName();

    /**
     * Optional list of additional arguments to pass to the generator.
     *
     * <p>See https://github.com/creek-service/creek-json-schema/tree/main/generator for more info.
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
