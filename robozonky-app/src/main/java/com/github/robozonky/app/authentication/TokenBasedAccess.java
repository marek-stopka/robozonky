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

package com.github.robozonky.app.authentication;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ServiceUnavailableException;

import com.github.robozonky.api.remote.entities.ZonkyApiToken;
import com.github.robozonky.common.remote.ApiProvider;
import com.github.robozonky.common.remote.Zonky;
import com.github.robozonky.common.secrets.SecretProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TokenBasedAccess extends AbstractAuthenticated {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBasedAccess.class);
    private final Supplier<Optional<ZonkyApiToken>> tokenSupplier;
    private final SecretProvider secrets;
    private final ApiProvider apis;

    TokenBasedAccess(final ApiProvider apis, final SecretProvider secrets, final Duration refreshAfter) {
        this.apis = apis;
        this.secrets = secrets;
        this.tokenSupplier = new ZonkyApiTokenSupplier(apis, secrets, refreshAfter);
    }

    private ZonkyApiToken getToken() {
        return tokenSupplier.get()
                .orElseThrow(() -> new ServiceUnavailableException("No API token available, authentication failed."));
    }

    @Override
    public <T> T call(final Function<Zonky, T> operation) {
        return call(operation, 1);
    }

    public <T> T call(final Function<Zonky, T> operation, final int attemptNo) {
        LOGGER.trace("Executing {}, attempt #{}.", operation, attemptNo);
        try {
            final T result = apis.authenticated(getToken(), operation);
            LOGGER.trace("Done with {}, attempt #{}.", operation, attemptNo);
            return result;
        } catch (final NotAuthorizedException ex) {
            if (attemptNo >= 3) {
                throw new IllegalStateException("There was a severe authorization problem.", ex);
            } else {
                LOGGER.debug("Request failed due to expired token, will retry #" + attemptNo);
                return call(operation, attemptNo + 1);
            }
        }
    }

    @Override
    public SecretProvider getSecretProvider() {
        return secrets;
    }
}
