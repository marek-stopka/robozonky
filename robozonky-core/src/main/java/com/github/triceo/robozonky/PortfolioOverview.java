/*
 * Copyright 2016 Lukáš Petrovický
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

package com.github.triceo.robozonky;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.triceo.robozonky.remote.Investment;
import com.github.triceo.robozonky.remote.OverallPortfolio;
import com.github.triceo.robozonky.remote.Rating;
import com.github.triceo.robozonky.remote.RiskPortfolio;
import com.github.triceo.robozonky.remote.Statistics;

public class PortfolioOverview {

    private static int sum(final Collection<Integer> vals) {
        return vals.stream().reduce(0, (a, b) -> a + b);
    }

    public static PortfolioOverview calculate(final BigDecimal balance, final Statistics stats,
                                              final Collection<Investment> investments) {
        // first figure out how much we have in outstanding loans
        final Map<Rating, Integer> amounts = stats.getRiskPortfolio().stream().collect(
                Collectors.toMap(RiskPortfolio::getRating, OverallPortfolio::getUnpaid)
        );
        // then make sure the share reflects investments made by ZonkyBot which have not yet been reflected in the API
        investments.forEach(previousInvestment -> {
            final Rating r = previousInvestment.getRating();
            amounts.put(r, amounts.get(r) + previousInvestment.getAmount());
        });
        return new PortfolioOverview(balance, Collections.unmodifiableMap(amounts));
    }

    private final int czkAvailable, czkInvested;
    private final Map<Rating, Integer> czkInvestedPerRating;
    private final Map<Rating, BigDecimal> sharesOnInvestment;

    private PortfolioOverview(final BigDecimal czkAvailable, final Map<Rating, Integer> czkInvestedPerRating) {
        this.czkAvailable = czkAvailable.intValue();
        this.czkInvested = PortfolioOverview.sum(czkInvestedPerRating.values());
        this.czkInvestedPerRating = czkInvestedPerRating;
        if (this.czkInvested == 0) {
            this.sharesOnInvestment = Collections.emptyMap();
        } else {
            this.sharesOnInvestment = Arrays.stream(Rating.values()).collect(Collectors.toMap(
                    Function.identity(),
                    r -> {
                        final BigDecimal invested = BigDecimal.valueOf(this.czkInvested);
                        final BigDecimal investedPerRating = BigDecimal.valueOf(this.getCzkInvested(r));
                        return investedPerRating.divide(invested, 4, RoundingMode.HALF_EVEN);
                    })
            );
        }
    }

    public int getCzkAvailable() {
        return this.czkAvailable;
    }

    public int getCzkInvested() {
        return this.czkInvested;
    }

    public int getCzkInvested(final Rating r) {
        return this.czkInvestedPerRating.getOrDefault(r, 0);
    }

    public BigDecimal getShareOnInvestment(final Rating r) {
        return this.sharesOnInvestment.getOrDefault(r, BigDecimal.ZERO);
    }

    public Map<Rating, BigDecimal> getSharesOnInvestment() {
        return this.sharesOnInvestment;
    }
}