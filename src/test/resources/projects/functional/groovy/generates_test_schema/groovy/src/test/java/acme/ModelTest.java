/*
 * Copyright 2024 Creek Contributors (https://github.com/creek-service)
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

package acme;

import org.junit.jupiter.api.Test;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;

class ModelTest {

    @Test
    void shouldLoadSchemaAsResource() {
        final Path path = Path.of(File.separator, "schema", "json", "acme.model.yml");
        final URL resource = ModelTest.class.getResource(path.toString());
        if (resource == null) {
            throw new AssertionError("resource not found: " + path);
        }
    }
}