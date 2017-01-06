/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.notifications.email;

import java.util.OptionalInt;

class ListenerSpecificNotificationProperties extends NotificationProperties {

    private final SupportedListener listener;

    public ListenerSpecificNotificationProperties(final SupportedListener listener,
                                                  final NotificationProperties props) {
        super(props.properties);
        this.listener = listener;
    }

    public int getListenerSpecificIntProperty(final String property, final int defaultValue) {
        return this.getListenerSpecificIntProperty(property).orElse(defaultValue);
    }

    public OptionalInt getListenerSpecificIntProperty(final String property) {
        return this.getIntValue(NotificationProperties.getCompositePropertyName(this.listener, property));
    }

}
