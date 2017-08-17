/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.strategy.natural;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import com.github.triceo.robozonky.api.strategies.StrategyService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class NaturalLanguageStrategyServiceTest {

    private static Function<InputStream, Optional<?>> getInvesting(final StrategyService s) {
        return s::toInvest;
    }

    private static Function<InputStream, Optional<?>> getPurchasing(final StrategyService s) {
        return s::toPurchase;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> parameters() {
        final Collection<Object[]> result = new ArrayList<>();
        final StrategyService service = new NaturalLanguageStrategyService();
        result.add(new Object[]{getInvesting(service)});
        result.add(new Object[]{getPurchasing(service)});
        return result;
    }

    @Parameterized.Parameter
    public Function<InputStream, Optional<Object>> strategyProvider;

    @Test
    public void test() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("only-whitespace");
        Assertions.assertThat(strategyProvider.apply(s)).isEmpty();
    }

    @Test
    public void complex() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("complex");
        Assertions.assertThat(strategyProvider.apply(s)).isPresent();
    }

    @Test
    public void simplest() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("simplest");
        Assertions.assertThat(strategyProvider.apply(s)).isPresent();
    }

    /**
     * This tests a real-life mistake. I forgot to end an expression with EOF - therefore the file was read to the
     * end without error, but whatever was written there was silently ignored. This resulted in an empty strategy,
     * leading the robot to invest into and purchase everything.
     */
    @Test
    public void missingHeaders() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("no-headers");
        Assertions.assertThat(strategyProvider.apply(s)).isEmpty();
    }

    @Test
    public void missingFilters1() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("missing-filters1");
        Assertions.assertThat(strategyProvider.apply(s)).isPresent();
    }

    @Test
    public void missingFilters2() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("missing-filters2");
        Assertions.assertThat(strategyProvider.apply(s)).isPresent();
    }

    @Test
    public void missingFilters3() {
        final InputStream s = NaturalLanguageStrategyServiceTest.class.getResourceAsStream("missing-filters3");
        Assertions.assertThat(strategyProvider.apply(s)).isPresent();
    }
}
