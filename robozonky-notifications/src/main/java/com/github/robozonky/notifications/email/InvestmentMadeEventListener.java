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

package com.github.robozonky.notifications.email;

import java.math.BigDecimal;
import java.util.Map;

import com.github.robozonky.api.notifications.InvestmentMadeEvent;
import com.github.robozonky.api.remote.entities.Investment;
import com.github.robozonky.util.FinancialCalculator;

class InvestmentMadeEventListener extends AbstractBalanceRegisteringEmailingListener<InvestmentMadeEvent> {

    public InvestmentMadeEventListener(final ListenerSpecificNotificationProperties properties) {
        super((i) -> i.getPortfolioOverview().getCzkAvailable(), properties);
    }

    @Override
    String getSubject(final InvestmentMadeEvent event) {
        final Investment i = event.getInvestment();
        return "Nová investice - " + i.getAmount().intValue() + ",- Kč, půjčka č. " + i.getLoanId();
    }

    @Override
    String getTemplateFileName() {
        return "investment-made.ftl";
    }

    @Override
    protected Map<String, Object> getData(final InvestmentMadeEvent event) {
        final Investment i = event.getInvestment();
        final Map<String, Object> result = Util.getLoanData(i);
        result.put("yield", FinancialCalculator.expectedInterest(i));
        final BigDecimal interestRate =
                FinancialCalculator.expectedInterestRateAfterFees(i, event.getPortfolioOverview());
        result.put("relativeYield", interestRate);
        result.put("isDryRun", event.isDryRun());
        result.put("newBalance", getNewBalance(event));
        return result;
    }
}
