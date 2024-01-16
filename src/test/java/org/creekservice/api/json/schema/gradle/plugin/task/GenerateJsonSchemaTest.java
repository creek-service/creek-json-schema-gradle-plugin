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

import static java.lang.System.lineSeparator;
import static org.creekservice.api.json.schema.gradle.plugin.GeneratorVersion.defaultGeneratorVersion;
import static org.creekservice.api.test.util.coverage.CodeCoverage.codeCoverageCmdLineArg;
import static org.creekservice.api.test.util.debug.RemoteDebug.remoteDebugArguments;
import static org.gradle.testkit.runner.TaskOutcome.FAILED;
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.creekservice.api.test.util.TestPaths;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.cartesian.ArgumentSets;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.MethodFactory;

@SuppressWarnings("ConstantConditions")
class GenerateJsonSchemaTest {

    // Change this to true locally to debug using attach-me plugin:
    private static final boolean DEBUG = false;

    private static final Path PROJECT_DIR = TestPaths.projectRoot("src");
    private static final Path BUILD_DIR = PROJECT_DIR.resolve("build").toAbsolutePath();
    private static final Path TEST_DIR =
            PROJECT_DIR.resolve("src/test/resources/projects/functional").toAbsolutePath();

    private static final String TASK_NAME = ":generateJsonSchema";
    private static final String TEST_TASK_NAME = ":generateTestJsonSchema";
    private static final String INIT_SCRIPT = "--init-script=" + TEST_DIR.resolve("init.gradle");

    @TempDir private Path projectDir;

    @BeforeEach
    void setUp() throws Exception {
        projectDir = projectDir.toRealPath();
        writeGradleProperties();
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldSkipIfNoMainSourceSet(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/no-source-sets");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SKIPPED));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldSkipIfCompileTasksDidNoWork(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/empty");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SKIPPED));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldExecuteWithDefaults(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/default");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SUCCESS));
        assertThat(
                result.getOutput(),
                containsString(
                        "--output-directory="
                                + projectDir.resolve(
                                        "build/generated/resources/schema/main/schema/json")));
        assertThat(result.getOutput(), containsString("--type-scanning-allowed-modules=<ANY>"));
        assertThat(result.getOutput(), containsString("--type-scanning-allowed-packages=<ANY>"));
        assertThat(result.getOutput(), containsString("--subtype-scanning-allowed-modules=<ANY>"));
        assertThat(result.getOutput(), containsString("--subtype-scanning-allowed-packages=<ANY>"));

        assertThat(
                result.getOutput(),
                containsString("JsonSchemaGenerator: " + defaultGeneratorVersion()));

        assertThat(
                result.getOutput(),
                matchesPattern(
                        Pattern.compile(
                                ".*--class-path=.*build/classes/java/main:.*", Pattern.DOTALL)));
        assertThat(
                "Should be running from the class-path",
                result.getOutput(),
                matchesPattern(
                        Pattern.compile(
                                ".*^--class-path=[^\n\r]*creek-json-schema-generator.*",
                                Pattern.MULTILINE | Pattern.DOTALL)));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldExecuteWithSpecificVersion(final String flavour, final String gradleVersion) {

        // Given:
        givenProject(flavour + "/specific_version");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SUCCESS));
        assertThat(result.getOutput(), containsString("JsonSchemaGenerator: 0.2.0-SNAPSHOT"));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldExecuteWithCustomProperties(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/fully_configured");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SUCCESS));
        assertThat(
                result.getOutput(),
                containsString(
                        "--output-directory=" + projectDir.resolve("build/custom/path/main/bob")));
        assertThat(
                result.getOutput(),
                containsString("--type-scanning-allowed-modules=[acme.test, acme.models]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--type-scanning-allowed-packages=[com.acme.test, com.acme.models]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--subtype-scanning-allowed-modules=[acme.test.sub, acme.models.sub]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--subtype-scanning-allowed-packages=[com.acme.test.sub,"
                                + " com.acme.models.sub]"));
        assertThat(
                "Should be running from the module-path",
                result.getOutput(),
                matchesPattern(
                        Pattern.compile(
                                ".*^--module-path=[^\n\r]*creek-json-schema-generator.*",
                                Pattern.MULTILINE | Pattern.DOTALL)));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldExecuteTestWithCustomProperties(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/fully_configured");

        // When:
        final BuildResult result = executeTask(TEST_TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TEST_TASK_NAME).getOutcome(), is(SUCCESS));
        assertThat(
                result.getOutput(),
                containsString(
                        "--output-directory=" + projectDir.resolve("build/custom/path/test/bob")));
        assertThat(
                result.getOutput(),
                containsString("--type-scanning-allowed-modules=[acme.test, acme.models]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--type-scanning-allowed-packages=[com.acme.test, com.acme.models]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--subtype-scanning-allowed-modules=[acme.test.sub, acme.models.sub]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--subtype-scanning-allowed-packages=[com.acme.test.sub,"
                                + " com.acme.models.sub]"));
        assertThat(
                "Should be running from the module-path",
                result.getOutput(),
                matchesPattern(
                        Pattern.compile(
                                ".*^--module-path=[^\n\r]*creek-json-schema-generator.*",
                                Pattern.MULTILINE | Pattern.DOTALL)));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldFailIfSystemTestConfigurationDoesNotContainExecutor(
            final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/missing_generator_dep");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.FAIL, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(FAILED));
        assertThat(
                result.getOutput(),
                containsString(
                        "No JSON schema generator dependency found in jsonSchemaGenerator"
                                + " configuration."));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldFailOnBadConfig(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/invalid_config");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.FAIL, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(FAILED));
        assertThat(result.getOutput(), containsString("Unknown option: '--unsupported-arg'"));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldExecuteWithOptions(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/empty");
        givenSourceFiles();

        // When:
        final BuildResult result =
                executeTask(
                        TASK_NAME,
                        ExpectedOutcome.PASS,
                        gradleVersion,
                        "--extra-argument=--echo-only",
                        "--type-scanning-allowed-module=acme.test",
                        "--type-scanning-allowed-module=acme.model",
                        "--type-scanning-allowed-package=org.test.base.one",
                        "--type-scanning-allowed-package=org.test.base.two",
                        "--subtype-scanning-allowed-module=acme.test.sub",
                        "--subtype-scanning-allowed-module=acme.model.sub",
                        "--subtype-scanning-allowed-package=org.test.sub.one",
                        "--subtype-scanning-allowed-package=org.test.sub.two");

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SUCCESS));
        assertThat(
                result.getOutput(),
                containsString("--type-scanning-allowed-modules=[acme.test, acme.model]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--type-scanning-allowed-packages=[org.test.base.one, org.test.base.two]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--subtype-scanning-allowed-modules=[acme.test.sub, acme.model.sub]"));
        assertThat(
                result.getOutput(),
                containsString(
                        "--subtype-scanning-allowed-packages=[org.test.sub.one,"
                                + " org.test.sub.two]"));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldRunGenerateTaskAfterCompileJavaAsPartOfProcessResources(
            final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/default");

        // When:
        final BuildResult result =
                executeTask(":processResources", ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.getOutput(), startsWith("> Task :compileJava"));
        assertThat(result.getOutput(), containsString("> Task :generateJsonSchema"));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldDeleteOutputDirectoryOnClean(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/default");
        final Path resultsDir =
                givenDirectory(
                        projectDir.resolve("build/generated/resources/schema/main/schema/json"));

        // When:
        final BuildResult result =
                executeTask(":cleanGenerateJsonSchema", ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(":cleanGenerateJsonSchema").getOutcome(), is(SUCCESS));
        assertThat(Files.exists(resultsDir), is(false));
    }

    @CartesianTest
    @MethodFactory("flavoursAndVersions")
    void shouldPlayNicelyWithOthers(final String flavour, final String gradleVersion) {
        // Given:
        givenProject(flavour + "/other_creek_plugin");

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SUCCESS));
    }

    @CartesianTest
    @MethodFactory("flavoursVersionsAndLanguage")
    void shouldWriteOutSchemaFiles(
            final String flavour, final String gradleVersion, final String language) {
        assumeTrue(supported(gradleVersion, language));

        // Given:
        givenProject(flavour + "/generates_schema/" + language);
        final Path expectedSchemaDir = givenDirectory(projectDir.resolve("expected"));
        final Path actualSchemaDir =
                givenDirectory(
                        projectDir.resolve("build/generated/resources/schema/main/schema/json"));

        // When:
        final BuildResult result = executeTask(TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TASK_NAME).getOutcome(), is(SUCCESS));
        assertSchemas(actualSchemaDir, expectedSchemaDir);
    }

    @CartesianTest
    @MethodFactory("flavoursVersionsAndLanguage")
    void shouldWriteOutTestSchemaFiles(
            final String flavour, final String gradleVersion, final String language)
            throws Exception {
        assumeTrue(supported(gradleVersion, language));

        // Given:
        givenProject(flavour + "/generates_schema/" + language);
        Files.move(
                projectDir.resolve("src").resolve("main"),
                projectDir.resolve("src").resolve("test"));
        final Path expectedSchemaDir = givenDirectory(projectDir.resolve("expected"));
        final Path actualSchemaDir =
                givenDirectory(
                        projectDir.resolve("build/generated/resources/schema/test/schema/json"));

        // When:
        final BuildResult result = executeTask(TEST_TASK_NAME, ExpectedOutcome.PASS, gradleVersion);

        // Then:
        assertThat(result.task(TEST_TASK_NAME).getOutcome(), is(SUCCESS));
        assertSchemas(actualSchemaDir, expectedSchemaDir);
    }

    @Test
    void shouldNotCheckInWithDebuggingEnabled() {
        assertThat("Do not check in with debugging enabled", !DEBUG);
    }

    private void givenProject(final String projectPath) {
        TestPaths.copy(TEST_DIR.resolve(projectPath), projectDir);
    }

    private Path givenDirectory(final Path path) {
        TestPaths.ensureDirectories(path);
        return path;
    }

    private void givenSourceFiles() {
        TestPaths.copy(TEST_DIR.resolve("groovy/default/src"), projectDir.resolve("src"));
    }

    private enum ExpectedOutcome {
        PASS,
        FAIL
    }

    private BuildResult executeTask(
            final String taskName,
            final ExpectedOutcome expectedOutcome,
            final String gradleVersion,
            final String... additionalArgs) {
        final List<String> args = new ArrayList<>(List.of(INIT_SCRIPT, "--stacktrace", taskName));
        args.addAll(List.of(additionalArgs));

        final GradleRunner runner =
                GradleRunner.create()
                        .withProjectDir(projectDir.toFile())
                        .withArguments(args)
                        .withPluginClasspath()
                        .withGradleVersion(gradleVersion);

        return expectedOutcome == ExpectedOutcome.FAIL ? runner.buildAndFail() : runner.build();
    }

    private void writeGradleProperties() {
        final List<String> options = new ArrayList<>(3);
        codeCoverageCmdLineArg(BUILD_DIR).ifPresent(options::add);

        if (DEBUG) {
            options.addAll(remoteDebugArguments());
        }

        if (!options.isEmpty()) {
            TestPaths.write(
                    projectDir.resolve("gradle.properties"),
                    "org.gradle.jvmargs=" + String.join(" ", options));
        }
    }

    private void assertSchemas(final Path actualSchemaDir, final Path expectedSchemaDir) {
        final List<Path> actualPaths =
                TestPaths.listDirectory(actualSchemaDir)
                        .sorted()
                        .collect(Collectors.toUnmodifiableList());
        final List<Path> expectedPaths =
                TestPaths.listDirectory(expectedSchemaDir)
                        .sorted()
                        .collect(Collectors.toUnmodifiableList());
        assertThat("Sanity check", expectedPaths, is(not(empty())));

        for (int idx = 0; idx < expectedPaths.size(); idx++) {
            final Path expectedPath = expectedPaths.get(idx);
            if (actualPaths.size() <= idx) {
                throw new AssertionError(
                        "Expected schema not output by task: " + expectedPath.toUri());
            }
            assertThat(readSchema(actualPaths.get(idx)), is(readSchema(expectedPath)));
        }

        if (actualPaths.size() > expectedPaths.size()) {
            final String unexpected =
                    actualPaths.subList(expectedPaths.size(), actualPaths.size()).stream()
                            .map(Path::toUri)
                            .map(URI::toString)
                            .collect(
                                    Collectors.joining(
                                            System.lineSeparator(), System.lineSeparator(), ""));
            throw new AssertionError("Unexpected schemas output by task: " + unexpected);
        }
    }

    private static String readSchema(final Path path) {
        try {
            return Files.readAllLines(path).stream()
                    .filter(line -> !line.startsWith("#"))
                    .collect(Collectors.joining(lineSeparator()));
        } catch (IOException e) {
            throw new AssertionError("Failed to read " + path.toUri());
        }
    }

    @SuppressWarnings("unused") // Invoked by reflection
    private static ArgumentSets flavoursAndVersions() {
        final Collection<?> flavours = List.of("kotlin", "groovy");
        // Note: update root README.md when updating this test dimension:
        final Collection<?> gradleVersions = List.of("6.4", "6.9.4", "7.6.1", "8.0.2");
        return ArgumentSets.argumentsForFirstParameter(flavours)
                .argumentsForNextParameter(gradleVersions);
    }

    @SuppressWarnings("unused") // Invoked by reflection
    private static ArgumentSets flavoursVersionsAndLanguage() {
        final Collection<?> languages = List.of("java", "java-module", "kotlin", "groovy");
        return flavoursAndVersions().argumentsForNextParameter(languages);
    }

    private static boolean supported(final String gradleVersion, final String language) {
        if (!language.endsWith("-module")) {
            return true;
        }

        // Gradle only properly supports modules from 7.0 onwards...
        final int majorVersion =
                Integer.parseInt(gradleVersion.substring(0, gradleVersion.indexOf(".")));
        return majorVersion > 6;
    }
}
