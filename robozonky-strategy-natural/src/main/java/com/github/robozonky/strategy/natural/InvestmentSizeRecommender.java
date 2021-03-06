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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;

import com.github.robozonky.api.remote.entities.Loan;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.internal.api.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InvestmentSizeRecommender implements BiFunction<Loan, Integer, Integer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentSizeRecommender.class);

    private final ParsedStrategy strategy;
    private final int maximumInvestmentAmount;

    public InvestmentSizeRecommender(final ParsedStrategy strategy, final int maximumInvestmentAmount) {
        this.strategy = strategy;
        this.maximumInvestmentAmount = maximumInvestmentAmount;
    }

    private static int roundToNearestIncrement(final int number) {
        return roundToNearestIncrement(number, Defaults.MINIMUM_INVESTMENT_INCREMENT_IN_CZK);
    }

    private static int roundToNearestIncrement(final int number, final int increment) {
        return (number / increment) * increment;
    }

    private static int getPercentage(final double original, final int percentage) {
        return BigDecimal.valueOf(original)
                .multiply(BigDecimal.valueOf(percentage))
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_EVEN)
                .intValue();
    }

    private int[] getInvestmentBounds(final ParsedStrategy strategy, final Loan loan) {
        final Rating rating = loan.getRating();
        final int absoluteMinimum = Math.max(strategy.getMinimumInvestmentSizeInCzk(rating),
                                             Defaults.MINIMUM_INVESTMENT_IN_CZK);
        final int minimumRecommendation = roundToNearestIncrement(absoluteMinimum);
        final int maximumUserRecommendation = roundToNearestIncrement(strategy.getMaximumInvestmentSizeInCzk(rating));
        if (maximumUserRecommendation > maximumInvestmentAmount) {
            LOGGER.info("Maximum investment amount reduced to {} by Zonky.", maximumInvestmentAmount);
        }
        final int maximumRecommendation = Math.min(maximumUserRecommendation, maximumInvestmentAmount);
        final int loanId = loan.getId();
        LOGGER.trace("Strategy gives investment range for loan #{} of <{}; {}> CZK.", loanId,
                     minimumRecommendation, maximumRecommendation);
        final int minimumInvestmentByShare =
                getPercentage(loan.getAmount(), strategy.getMinimumInvestmentShareInPercent());
        final int minimumInvestment =
                Math.max(minimumInvestmentByShare, strategy.getMinimumInvestmentSizeInCzk(loan.getRating()));
        final int maximumInvestmentByShare =
                getPercentage(loan.getAmount(), strategy.getMaximumInvestmentShareInPercent());
        final int maximumInvestment =
                Math.min(maximumInvestmentByShare, strategy.getMaximumInvestmentSizeInCzk(loan.getRating()));
        // minimums are guaranteed to be <= maximums due to the contract of strategy implementation
        return new int[]{minimumInvestment, maximumInvestment};
    }

    @Override
    public Integer apply(final Loan loan, final Integer balance) {
        final int id = loan.getId();
        final int[] recommended = getInvestmentBounds(strategy, loan);
        final int minimumRecommendation = recommended[0];
        final int maximumRecommendation = recommended[1];
        LOGGER.debug("Recommended investment range for loan #{} is <{}; {}> CZK.", id, minimumRecommendation,
                     maximumRecommendation);
        // round to nearest lower increment
        final int loanRemaining = (int) loan.getRemainingInvestment();
        if (minimumRecommendation > balance) {
            LOGGER.debug("Not recommending loan #{} due to minimum over balance.", id);
            return 0;
        } else if (minimumRecommendation > loanRemaining) {
            LOGGER.debug("Not recommending loan #{} due to minimum over remaining.", id);
            return 0;
        }
        final int recommendedAmount = Math.min(balance, Math.min(maximumRecommendation, loanRemaining));
        final int r = roundToNearestIncrement(recommendedAmount);
        if (r < minimumRecommendation) {
            LOGGER.debug("Not recommending loan #{} due to recommendation below minimum.", id);
            return 0;
        } else {
            LOGGER.debug("Final recommendation for loan #{} is {} CZK.", id, r);
            return r;
        }
    }
}
