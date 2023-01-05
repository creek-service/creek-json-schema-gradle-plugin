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

import static org.creekservice.api.json.schema.gradle.plugin.GeneratorVersion.defaultGeneratorVersion;
import static org.creekservice.api.json.schema.gradle.plugin.GeneratorVersion.loadResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GeneratorVersionTest {
    @Test
    void shouldLoadVersion() {
        assertThat(defaultGeneratorVersion(), is(not("")));
    }

    @Test
    void shouldThrowIfResourceNotFound() {
        // When:
        final Exception e =
                assertThrows(IllegalStateException.class, () -> loadResource("wont_find_me"));

        // Then:
        assertThat(e.getMessage(), is("Jar does not contain wont_find_me resource"));
    }
}
