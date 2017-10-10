// Copyright 2014 The Serviced Authors.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


package org.zenoss.app.autobundle;

import com.google.common.base.Optional;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import org.zenoss.app.AppConfiguration;

/**
 * Interface for configuring a drop wizard bundle that can be automatically loaded.
 */
public interface AutoConfiguredBundle<T extends AppConfiguration> {

    /**
     * Get the initialized bundle to be loaded.
     *
     * @param bootstrap The {@link Bootstrap} instance from which the bundle
     *                  is retrieved.
     * @return ConfiguredBundle to be added.
     */
    ConfiguredBundle getBundle(Bootstrap bootstrap);

    /**
     * Asserts that the class returned should be the same, a superclass or a super interface of the App Configuration
     *
     * @return Class Configuration type expected by the bundle
     */
    Optional<Class<T>> getRequiredConfig();

}
