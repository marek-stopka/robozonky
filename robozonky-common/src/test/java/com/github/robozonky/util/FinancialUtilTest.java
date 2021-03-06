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

package com.github.robozonky.util;

import java.math.BigDecimal;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;
import org.junit.Test;

public class FinancialUtilTest {

    @Test
    public void ipmt() {
        final BigDecimal rate = new BigDecimal("0.1");
        final BigDecimal principal = BigDecimal.valueOf(100);
        final int maxTerm = 10;
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(FinancialUtil.ipmt(rate, 1, maxTerm, principal).toEngineeringString())
                    .isEqualTo(BigDecimal.TEN.negate().toEngineeringString());
            softly.assertThat(FinancialUtil.ipmt(rate, maxTerm, maxTerm, principal)).isLessThan(BigDecimal.ZERO);
        });
    }

    @Test
    public void ppmt() {
        final BigDecimal rate = new BigDecimal("0.1");
        final BigDecimal principal = BigDecimal.valueOf(100);
        final int maxTerm = 10;
        final BigDecimal result = IntStream.range(0, maxTerm)
                .mapToObj(term -> FinancialUtil.ppmt(rate, term + 1, maxTerm, principal))
                .reduce(BigDecimal.ZERO, BigDecimalCalculator::plus);
        Assertions.assertThat(result).isCloseTo(principal.negate(), Percentage.withPercentage(0.01));
    }

    @Test
    public void pmt() {
        final BigDecimal rate = new BigDecimal("0.1");
        final BigDecimal principal = BigDecimal.valueOf(100);
        final int maxTerm = 10;
        final BigDecimal result = IntStream.range(0, maxTerm)
                .mapToObj(term -> FinancialUtil.pmt(rate, maxTerm, principal))
                .reduce(BigDecimal.ZERO, BigDecimalCalculator::plus);
        Assertions.assertThat(result).isCloseTo(BigDecimal.valueOf(-162), Percentage.withPercentage(0.5));
    }
}
