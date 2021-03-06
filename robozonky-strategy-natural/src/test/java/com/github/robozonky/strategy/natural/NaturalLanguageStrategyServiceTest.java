/*
 * Copyright 2017 The RoboZonky Project
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

package com.github.robozonky.strategy.natural;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.robozonky.api.strategies.StrategyService;
import com.github.robozonky.internal.api.Defaults;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class NaturalLanguageStrategyServiceTest {

    private static final StrategyService SERVICE = new NaturalLanguageStrategyService();
    @Parameterized.Parameter
    public Type strategyProvider;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.stream(Type.values())
                .map(t -> new Object[]{t})
                .collect(Collectors.toSet());
    }

    private Optional<?> getStrategy(final String str) {
        return strategyProvider.getStrategy().apply(str);
    }

    @Test
    public void test() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("only-whitespace");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        Assertions.assertThat(getStrategy(str)).isEmpty();
    }

    @Test
    public void newlines() { // test all forms of line endings known to man
        final String str = new String("Robot má udržovat balancované portfolio.\n \r \r\n");
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    @Test
    public void windows1250WindowsNewlines() throws IOException {
        // https://github.com/RoboZonky/robozonky/issues/181#issuecomment-346653495
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("newlines-ansi-windows");
        final String str = IOUtils.toString(s, Charset.forName("windows-1250"));
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    @Test
    public void windows1250UnixNewlines() throws IOException {
        // https://github.com/RoboZonky/robozonky/issues/181#issuecomment-346653495
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("newlines-ansi-unix");
        final String str = IOUtils.toString(s, Charset.forName("windows-1250"));
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    @Test
    public void complex() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("complex");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        Assertions.assertThat(getStrategy(str)).isPresent();
    }

    @Test
    public void complexWithWhitespace() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("complex-whitespace");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        Assertions.assertThat(getStrategy(str)).isPresent();
    }

    @Test
    public void simplest() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("simplest");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    @Test
    public void disabled() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("disabled-filters");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        Assertions.assertThat(getStrategy(str)).isEmpty();
    }

    @Test
    public void enabled() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("enabled-filters");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    /**
     * This tests a real-life mistake. I forgot to end an expression with EOF - therefore the file was read to the
     * end without error, but whatever was written there was silently ignored. This resulted in an empty strategy,
     * leading the robot to invest into and purchase everything.
     */
    @Test
    public void missingHeaders() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("no-headers");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        Assertions.assertThat(getStrategy(str)).isEmpty();
    }

    @Test
    public void missingFilters1() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("missing-filters1");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    @Test
    public void missingFilters2() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("missing-filters2");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        Assertions.assertThat(getStrategy(str)).isPresent();
    }

    @Test
    public void missingFilters3() throws IOException {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("missing-filters3");
        final String str = IOUtils.toString(s, Defaults.CHARSET);
        if (strategyProvider == Type.SELLING) {
            Assertions.assertThat(getStrategy(str)).isEmpty();
        } else {
            Assertions.assertThat(getStrategy(str)).isPresent();
        }
    }

    private enum Type {

        INVESTING {
            @Override
            public Function<String, Optional<?>> getStrategy() {
                return SERVICE::toInvest;
            }
        },
        PURCHASING {
            @Override
            public Function<String, Optional<?>> getStrategy() {
                return SERVICE::toPurchase;
            }
        },
        SELLING {
            @Override
            public Function<String, Optional<?>> getStrategy() {
                return SERVICE::toSell;
            }
        };

        public abstract Function<String, Optional<?>> getStrategy();

    }
}

