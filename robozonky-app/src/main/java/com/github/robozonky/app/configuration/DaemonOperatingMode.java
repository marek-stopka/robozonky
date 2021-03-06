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

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.github.robozonky.app.authentication.Authenticated;
import com.github.robozonky.app.configuration.daemon.BlockedAmountsUpdater;
import com.github.robozonky.app.configuration.daemon.DaemonInvestmentMode;
import com.github.robozonky.app.configuration.daemon.PortfolioUpdater;
import com.github.robozonky.app.configuration.daemon.StrategyProvider;
import com.github.robozonky.app.investing.Investor;
import com.github.robozonky.app.portfolio.Delinquents;
import com.github.robozonky.app.portfolio.Selling;
import com.github.robozonky.common.extensions.MarketplaceLoader;
import com.github.robozonky.common.secrets.Credentials;
import com.github.robozonky.common.secrets.SecretProvider;
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
        final SecretProvider secretProvider = auth.getSecretProvider();
        final Credentials cred = new Credentials(marketplace.getMarketplaceCredentials(), secretProvider);
        return MarketplaceLoader.load(cred)
                .map(marketplaceImpl -> {
                    final PortfolioUpdater updater = new PortfolioUpdater(auth);
                    final BlockedAmountsUpdater bau = new BlockedAmountsUpdater(auth, updater);
                    // run update of blocked amounts automatically with every portfolio update
                    updater.registerDependant(bau.getDependant());
                    // update delinquents automatically with every portfolio update
                    updater.registerDependant((p, a) -> Delinquents.update(a, p));
                    final StrategyProvider sp = StrategyProvider.createFor(strategy.getStrategyLocation());
                    // attempt to sell participations after every portfolio update
                    updater.registerDependant(new Selling(sp::getToSell, builder.isDryRun()));
                    final InvestmentMode m = new DaemonInvestmentMode(auth, updater, builder, isFaultTolerant,
                                                                      marketplaceImpl, sp, bau,
                                                                      marketplace.getMaximumSleepDuration(),
                                                                      marketplace.getPrimaryMarketplaceCheckDelay(),
                                                                      marketplace.getSecondaryMarketplaceCheckDelay());
                    // only schedule internal data updates after daemon had a chance to initialize...
                    final Scheduler scheduler = Scheduler.inBackground();
                    final Future<?> f = scheduler.run(updater);
                    try {
                        /*
                         * wait for the update to finish; has to be done in this roundabout way, so that integration
                         * tests can substitute this operation, which would otherwise call a live Zonky API, by a no-op
                         * via the pluggable scheduler mechanism.
                         */
                        f.get();
                    } catch (final ExecutionException | InterruptedException ex) {
                        LOGGER.error("Failed updating portfolio.", ex);
                        return Optional.<InvestmentMode>empty();
                    }
                    return Optional.of(m);
                }).orElse(Optional.empty());
    }
}
