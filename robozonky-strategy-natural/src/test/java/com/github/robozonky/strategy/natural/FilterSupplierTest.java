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

import java.util.Collections;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class FilterSupplierTest {

    @Test
    public void nullHandling() {
        final FilterSupplier f = new FilterSupplier(new DefaultValues(DefaultPortfolio.PROGRESSIVE), null, null,
                                                    Collections.emptyList());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(f.getPrimaryMarketplaceFilters()).isEmpty();
            softly.assertThat(f.getSecondaryMarketplaceFilters()).isEmpty();
            softly.assertThat(f.getSellFilters()).isEmpty();
        });
    }
}
