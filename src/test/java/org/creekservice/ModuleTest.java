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

package org.creekservice;


import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckConstructorsPrivate;
import org.creekservice.api.test.conformity.check.CheckModule;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldConform() {
        ConformityTester.builder(ModuleTest.class)
                .withDisabled("Gradle doesn't support modular plugins yet", CheckModule.builder())
                .withDisabled(
                        "Gradle requires public constructors", CheckConstructorsPrivate.builder())
                .check();
    }
}
