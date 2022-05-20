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

    public static final String CONFIG_DYNAMIC_MAX_FACTOR_PROPERTY_NAME = "dynamic.max.factor";

    @Override
    public ParallelExecutionConfiguration createConfiguration(
            final ConfigurationParameters configurationParameters) {
        final BigDecimal factor =
                getFactor(
                        CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME,
                        configurationParameters,
                        f -> f.compareTo(BigDecimal.ZERO) > 0);

        final BigDecimal maxFactor =
                getFactor(
                        CONFIG_DYNAMIC_MAX_FACTOR_PROPERTY_NAME,
                        configurationParameters,
                        f -> f.compareTo(BigDecimal.ONE) >= 0);

        final BigDecimal processors =
                BigDecimal.valueOf(Runtime.getRuntime().availableProcessors());
        final int parallelism = Math.max(1, factor.multiply(processors).intValue());
        final int maxThreadCount = maxFactor.multiply(processors).intValue();

        return new CreekParallelExecutionConfiguration(
                parallelism, maxThreadCount, KEEP_ALIVE_SECONDS);
    }

    private BigDecimal getFactor(
            final String configName,
            final ConfigurationParameters configurationParameters,
            final Predicate<BigDecimal> check) {
        final BigDecimal factor =
                configurationParameters.get(configName, BigDecimal::new).orElse(BigDecimal.ONE);

        Preconditions.condition(
                check.test(factor),
                () ->
                        String.format(
                                "Factor '%s' specified via configuration parameter '%s' must be greater than 0",
                                factor, configName));
        return factor;
    }

    private static class CreekParallelExecutionConfiguration
            implements ParallelExecutionConfiguration {

        private final int parallelism;
        private final int maxThreadCount;
        private final int keepAliveSeconds;

        CreekParallelExecutionConfiguration(
                final int parallelism, final int maxThreadCount, final int keepAliveSeconds) {
            this.parallelism = parallelism;
            this.maxThreadCount = maxThreadCount;
            this.keepAliveSeconds = keepAliveSeconds;
        }

        @Override
        public int getParallelism() {
            return parallelism;
        }

        @Override
        public int getMinimumRunnable() {
            return parallelism;
        }

        @Override
        public int getMaxPoolSize() {
            return maxThreadCount;
        }

        @Override
        public int getCorePoolSize() {
            return parallelism;
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
                System.out.println("pool runningThreadCount:" + pool.getRunningThreadCount());
                System.out.println("pool quoteTaskCount:" + pool.getQueuedTaskCount());
                return true;
            };
        }
    }
}
