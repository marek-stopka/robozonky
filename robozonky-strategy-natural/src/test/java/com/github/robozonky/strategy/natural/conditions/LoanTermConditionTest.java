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

package com.github.robozonky.strategy.natural.conditions;

import com.github.robozonky.strategy.natural.Wrapper;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.mockito.Mockito;

public class LoanTermConditionTest {

    @Test
    public void leftBoundWrong() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> new LoanTermCondition(-1, 0)).isInstanceOf(IllegalArgumentException.class);
            softly.assertThatThrownBy(() -> new LoanTermCondition(0, -1)).isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void rightBoundWrong() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThatThrownBy(() -> new LoanTermCondition(85, 0)).isInstanceOf(IllegalArgumentException.class);
            softly.assertThatThrownBy(() -> new LoanTermCondition(0, 85)).isInstanceOf(IllegalArgumentException.class);
        });
    }

    @Test
    public void boundaryCorrect() {
        final Wrapper l = Mockito.mock(Wrapper.class);
        Mockito.when(l.getRemainingTermInMonths()).thenReturn(1);
        final LoanTermCondition condition = new LoanTermCondition(1, 1);
        Assertions.assertThat(condition.test(l)).isTrue();
    }

    @Test
    public void leftOutOfBounds() {
        final Wrapper l = Mockito.mock(Wrapper.class);
        Mockito.when(l.getRemainingTermInMonths()).thenReturn(0);
        final LoanTermCondition condition = new LoanTermCondition(1, 1);
        Assertions.assertThat(condition.test(l)).isFalse();
    }

    @Test
    public void rightOutOfBounds() {
        final Wrapper l = Mockito.mock(Wrapper.class);
        Mockito.when(l.getRemainingTermInMonths()).thenReturn(2);
        final LoanTermCondition condition = new LoanTermCondition(1, 1);
        Assertions.assertThat(condition.test(l)).isFalse();
    }
}
