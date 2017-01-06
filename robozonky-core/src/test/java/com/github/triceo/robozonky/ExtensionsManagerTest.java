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

package com.github.triceo.robozonky;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import com.github.triceo.robozonky.api.notifications.ListenerService;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ExtensionsManagerTest {

    @Test
    public void noExtensions() {
        final ServiceLoader<ListenerService> result = ExtensionsManager.INSTANCE.getServiceLoader(ListenerService.class);
        Assertions.assertThat(result).isNotNull();
    }

    @Test
    public void loadJarsFromFolderWithJars() {
        final File f = new File("target");
        Assertions.assertThat(ExtensionsManager.INSTANCE.retrieveExtensionClassLoader(f))
                .isInstanceOf(URLClassLoader.class);
    }

    @Test
    public void loadJarsFromFolderWithNoJars() {
        final File f = new File("src");
        Assertions.assertThat(ExtensionsManager.INSTANCE.retrieveExtensionClassLoader(f))
                .isInstanceOf(URLClassLoader.class);
    }

    @Test
    public void loadJarsFromNonExistentFolder() {
        Assertions.assertThat(ExtensionsManager.INSTANCE.retrieveExtensionClassLoader())
                .isSameAs(ExtensionsManager.class.getClassLoader());
    }

}
