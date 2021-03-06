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

package com.github.robozonky.internal.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.github.robozonky.api.remote.enums.Rating;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

public class SettingsTest {

    private static final class TemporalPredicate implements Predicate<TemporalAmount> {

        private final long seconds;

        public TemporalPredicate(final long seconds) {
            this.seconds = seconds;
        }

        @Override
        public boolean test(final TemporalAmount o) {
            return o.get(ChronoUnit.SECONDS) == seconds;
        }

        @Override
        public String toString() {
            return "seconds = " + seconds;
        }
    }

    @Rule
    public final RestoreSystemProperties propertiesRestorer = new RestoreSystemProperties();

    @After
    public void reinit() {
        Settings.INSTANCE.reinit();
    }

    @Test
    public void defaultProperties() {
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(Settings.INSTANCE.get("user.dir", "")).isNotEqualTo("");
            softly.assertThat(Settings.INSTANCE.get(UUID.randomUUID().toString(), ""))
                    .isEqualTo("");
            softly.assertThat(Settings.INSTANCE.isDebugEventStorageEnabled()).isFalse();
            softly.assertThat(Settings.INSTANCE.getTokenRefreshPeriod())
                    .matches(new SettingsTest.TemporalPredicate(60));
            softly.assertThat(Settings.INSTANCE.getRemoteResourceRefreshInterval())
                    .matches(new SettingsTest.TemporalPredicate(5 * 60));
            softly.assertThat(Settings.INSTANCE.getCaptchaDelay())
                    .matches(new SettingsTest.TemporalPredicate(0));
            Stream.of(Rating.values()).forEach(r -> {
                softly.assertThat(Settings.INSTANCE.getCaptchaDelay(r))
                        .as(r.toString())
                        .matches(new SettingsTest.TemporalPredicate(0));
            });
            softly.assertThat(Settings.INSTANCE.getDefaultDryRunBalance()).isEqualTo(-1);
            softly.assertThat(Settings.INSTANCE.getSocketTimeout())
                    .matches(new SettingsTest.TemporalPredicate(60));
            softly.assertThat(Settings.INSTANCE.getConnectionTimeout())
                    .matches(new SettingsTest.TemporalPredicate(60));
            softly.assertThat(Settings.INSTANCE.getDefaultApiPageSize()).isEqualTo(100);
        });
    }

    @Test
    public void setProperties() throws IOException {
        final Properties p = new Properties();
        Stream.of(Settings.Key.values()).forEach(v -> p.setProperty(v.getName(), "1000"));
        final File f = File.createTempFile("robozonky-", ".properties");
        p.store(new FileWriter(f), "Testing properties");
        System.setProperty(Settings.FILE_LOCATION_PROPERTY, f.getAbsolutePath());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(Settings.INSTANCE.get("user.dir", "")).isNotEqualTo("");
            softly.assertThat(Settings.INSTANCE.get(UUID.randomUUID().toString(), ""))
                    .isEqualTo("");
            softly.assertThat(Settings.INSTANCE.isDebugEventStorageEnabled()).isFalse();
            softly.assertThat(Settings.INSTANCE.isDebugHttpResponseLoggingEnabled()).isFalse();
            softly.assertThat(Settings.INSTANCE.getTokenRefreshPeriod())
                    .matches(new SettingsTest.TemporalPredicate(1000));
            softly.assertThat(Settings.INSTANCE.getRemoteResourceRefreshInterval())
                    .matches(new SettingsTest.TemporalPredicate(1000 * 60));
            softly.assertThat(Settings.INSTANCE.getCaptchaDelay())
                    .matches(new SettingsTest.TemporalPredicate(1000));
            Stream.of(Rating.values()).forEach(r -> {
                softly.assertThat(Settings.INSTANCE.getCaptchaDelay(r))
                        .matches(new SettingsTest.TemporalPredicate(1000));
            });
            softly.assertThat(Settings.INSTANCE.getDefaultDryRunBalance()).isEqualTo(1000);
            softly.assertThat(Settings.INSTANCE.getSocketTimeout())
                    .matches(new SettingsTest.TemporalPredicate(1000));
            softly.assertThat(Settings.INSTANCE.getConnectionTimeout())
                    .matches(new SettingsTest.TemporalPredicate(1000));
            softly.assertThat(Settings.INSTANCE.getDefaultApiPageSize()).isEqualTo(1000);
        });
    }
}
