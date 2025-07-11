/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

import com.google.common.base.Preconditions;
import org.creekservice.api.base.annotation.schema.GeneratesSchema;

@GeneratesSchema
public final class Other {

    public Other(final String s) {
        // Use dependency so that class would fail to load if dependencies were missing
        Preconditions.checkArgument(s != null);
    }

    public long getSomeProp() {
        return 1L;
    }
}