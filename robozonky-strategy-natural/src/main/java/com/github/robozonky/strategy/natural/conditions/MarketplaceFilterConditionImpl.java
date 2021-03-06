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

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class MarketplaceFilterConditionImpl implements MarketplaceFilterCondition {

    // not static as we want to have the specific impl class in the logs
    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        final String description = getDescription().orElse("N/A.");
        return this.getClass().getSimpleName() + " (" + description + ")";
    }

    @Override
    public MarketplaceFilterCondition negate() {
        if (this instanceof NegatingCondition) {
            return ((NegatingCondition) this).getToNegate();
        } else {
            return new NegatingCondition(this);
        }
    }
}
