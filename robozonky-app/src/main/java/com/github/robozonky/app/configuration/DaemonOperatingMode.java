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

package com.github.robozonky.app.configuration;

import java.time.Duration;
import java.util.Optional;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.robozonky.app.authentication.Authenticated;
import com.github.robozonky.app.configuration.daemon.DaemonInvestmentMode;
import com.github.robozonky.app.investing.Investor;
import com.github.robozonky.app.portfolio.PortfolioUpdater;
import com.github.robozonky.common.extensions.MarketplaceLoader;
import com.github.robozonky.common.secrets.Credentials;
import com.github.robozonky.util.Scheduler;

@Parameters(commandNames = "daemon", commandDescription = "Constantly checks marketplaces, invests based on strategy.")
class DaemonOperatingMode extends OperatingMode {

    @ParametersDelegate
    MarketplaceCommandLineFragment marketplace = new MarketplaceCommandLineFragment();

    @ParametersDelegate
    StrategyCommandLineFragment strategy = new StrategyCommandLineFragment();

    @Override
    protected Optional<InvestmentMode> getInvestmentMode(final CommandLine cli, final Authenticated auth,
                                                         final Investor.Builder builder) {
        final boolean isFaultTolerant = cli.getTweaksFragment().isFaultTolerant();
        final Credentials cred = new Credentials(marketplace.getMarketplaceCredentials(),
                                                 auth.getSecretProvider());
        return MarketplaceLoader.load(cred)
                .map(marketplaceImpl -> {
                    final InvestmentMode m = new DaemonInvestmentMode(auth, builder, isFaultTolerant, marketplaceImpl,
                                                                      strategy.getStrategyLocation(),
                                                                      marketplace.getMaximumSleepDuration(),
                                                                      marketplace.getPrimaryMarketplaceCheckDelay(),
                                                                      marketplace.getSecondaryMarketplaceCheckDelay());
                    // only schedule internal data updates after daemon had a chance to initialize
                    Scheduler.inBackground().submit(new PortfolioUpdater(auth), Duration.ofHours(1));
                    return Optional.of(m);
                }).orElse(Optional.empty());
    }
}
