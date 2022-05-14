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

plugins {
    id("org.creekservice.json.schema")
    `java-library`
}

dependencies {
    api("org.creekservice:creek-base-annotation:0.2.0-SNAPSHOT")
    implementation("com.google.guava:guava:31.1-jre")
}

creek.schema.json {
    typeScanning.moduleWhiteList.set(listOf("acme.models"))
}