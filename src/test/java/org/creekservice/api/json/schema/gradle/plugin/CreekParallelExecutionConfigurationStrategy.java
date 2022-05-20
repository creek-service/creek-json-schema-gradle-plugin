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

import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;

import java.math.BigDecimal;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class CreekParallelExecutionConfigurationStrategy
        implements ParallelExecutionConfigurationStrategy {
    private static final int KEEP_ALIVE_SECONDS = 30;

    @Override
    public ParallelExecutionConfiguration createConfiguration(
            final ConfigurationParameters configurationParameters) {
        final BigDecimal factor =
                configurationParameters
                        .get(CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME, BigDecimal::new)
                        .orElse(BigDecimal.ONE);

        Preconditions.condition(
                factor.compareTo(BigDecimal.ZERO) > 0,
                () ->
                        String.format(
                                "Factor '%s' specified via configuration parameter '%s' must be greater than 0",
                                factor, CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME));

        final int parallelism =
                Math.max(
                        1,
                        factor.multiply(
                                        BigDecimal.valueOf(
                                                Runtime.getRuntime().availableProcessors()))
                                .intValue());

        return new DefaultParallelExecutionConfiguration(
                parallelism, parallelism, parallelism, parallelism, KEEP_ALIVE_SECONDS);
    }

    private static class DefaultParallelExecutionConfiguration
            implements ParallelExecutionConfiguration {

        private final int parallelism;
        private final int minimumRunnable;
        private final int maxPoolSize;
        private final int corePoolSize;
        private final int keepAliveSeconds;

        DefaultParallelExecutionConfiguration(
                final int parallelism,
                final int minimumRunnable,
                final int maxPoolSize,
                final int corePoolSize,
                final int keepAliveSeconds) {
            this.parallelism = parallelism;
            this.minimumRunnable = minimumRunnable;
            this.maxPoolSize = maxPoolSize;
            this.corePoolSize = corePoolSize;
            this.keepAliveSeconds = keepAliveSeconds;
        }

        @Override
        public int getParallelism() {
            return parallelism;
        }

        @Override
        public int getMinimumRunnable() {
            return minimumRunnable;
        }

        @Override
        public int getMaxPoolSize() {
            return maxPoolSize;
        }

        @Override
        public int getCorePoolSize() {
            return corePoolSize;
        }

        @Override
        public int getKeepAliveSeconds() {
            return keepAliveSeconds;
        }

        @Override
        public Predicate<? super ForkJoinPool> getSaturatePredicate() {
            // Todo:
            return pool -> {
                System.out.println("pool parallelism:" + pool.getParallelism());
                System.out.println("pool activeThreadCount:" + pool.getActiveThreadCount());
                System.out.println("pool poolSize:" + pool.getPoolSize());
                System.out.println("pool poolSize:" + pool.getPoolSize());
                return true;
            };
        }
    }
}
