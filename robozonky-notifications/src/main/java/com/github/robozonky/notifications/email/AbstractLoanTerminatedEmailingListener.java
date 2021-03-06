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

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

import com.github.robozonky.api.notifications.Event;
import com.github.robozonky.api.remote.entities.Loan;

abstract class AbstractLoanTerminatedEmailingListener<T extends Event> extends AbstractEmailingListener<T> {

    private final Function<T, Loan> loanSupplier;
    private final Function<T, LocalDate> dateSupplier;

    protected AbstractLoanTerminatedEmailingListener(final Function<T, Loan> loanSupplier,
                                                     final Function<T, LocalDate> dateSupplier,
                                                     final ListenerSpecificNotificationProperties properties) {
        super(properties);
        this.loanSupplier = loanSupplier;
        this.dateSupplier = dateSupplier;
        registerFinisher(event -> DelinquencyTracker.INSTANCE.unsetDelinquent(loanSupplier.apply(event)));
    }

    @Override
    boolean shouldSendEmail(final T event) {
        return super.shouldSendEmail(event) && DelinquencyTracker.INSTANCE.isDelinquent(loanSupplier.apply(event));
    }

    @Override
    protected Map<String, Object> getData(final T event) {
        final Loan loan = loanSupplier.apply(event);
        final Map<String, Object> result = Util.getLoanData(loan);
        result.put("since", Util.toDate(dateSupplier.apply(event)));
        return result;
    }
}
