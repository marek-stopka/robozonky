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

package com.github.robozonky.installer.panels;

import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.installer.DataValidator;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class AbstractValidatorTest {

    private static class TestValidator extends AbstractValidator {

        @Override
        public String getErrorMessageId() {
            return null;
        }

        @Override
        public String getWarningMessageId() {
            return null;
        }

        @Override
        protected DataValidator.Status validateDataPossiblyThrowingException(final InstallData installData) {
            throw new IllegalStateException();
        }
    }

    @Test
    public void doesNotThrow() {
        final AbstractValidator v = new TestValidator();
        Assertions.assertThat(v.validateData(null)).isEqualTo(DataValidator.Status.ERROR);
    }
}
