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

import com.beust.jcommander.Parameter;

class TweaksCommandLineFragment extends AbstractCommandLineFragment {

    @Parameter(names = {"-d", "--dry"},
            description = "RoboZonky will simulate investments, but never actually spend money.")
    private boolean dryRunEnabled = false;

    @Parameter(names = {"-t", "--fault-tolerant"},
            description = "RoboZonky will not report errors if it thinks Zonky is down. Use with caution.")
    private boolean faultTolerant = false;

    public boolean isDryRunEnabled() {
        return dryRunEnabled;
    }

    public boolean isFaultTolerant() {
        return faultTolerant;
    }
}
