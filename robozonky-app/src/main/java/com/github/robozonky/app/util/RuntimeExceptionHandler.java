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

package com.github.robozonky.app.util;

import java.util.function.Consumer;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.WebApplicationException;

public abstract class RuntimeExceptionHandler {

    protected abstract Consumer<Throwable> getCommunicationFailureHandler();

    protected abstract Consumer<Throwable> getRemoteFailureHandler();

    protected abstract Consumer<Throwable> getOtherFailureHandler();

    private Consumer<Throwable> getHandler(final Throwable t) {
        if (t instanceof ProcessingException || t instanceof NotAllowedException || t instanceof ServerErrorException) {
            return this.getCommunicationFailureHandler();
        } else if (t instanceof WebApplicationException) {
            return this.getRemoteFailureHandler();
        }
        return this.getOtherFailureHandler();
    }

    public void handle(final Throwable t) {
        getHandler(t).accept(t);
    }
}
