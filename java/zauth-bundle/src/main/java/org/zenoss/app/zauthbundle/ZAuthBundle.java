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

package org.zenoss.app.zauthbundle;

import com.google.common.base.Optional;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.zenoss.app.AppConfiguration;
import org.zenoss.app.annotations.Bundle;
import org.zenoss.app.autobundle.AutoConfiguredBundle;
import org.zenoss.app.config.ProxyConfiguration;

import java.util.Map;
import java.util.Random;

/**
 * Configured bundle that sets up shiro authentication for the zapp http requests.
 */
@Bundle
public class ZAuthBundle implements AutoConfiguredBundle<AppConfiguration> {

    @Override
    public ConfiguredBundle getBundle(Bootstrap bootstrap) {
        return new ZAuthShiroBundle(bootstrap.getApplication().getName());
    }

    @Override
    public Optional<Class<AppConfiguration>> getRequiredConfig() {
        return Optional.of(AppConfiguration.class);
    }

    /**
     * This class initializes shiro in our environment.
     */
    static class ZAuthShiroBundle implements ConfiguredBundle<AppConfiguration> {

        private static final String URL_PATTERN = "/*";
        private final String name;

        public ZAuthShiroBundle(String name) {
            this.name = name.replace(" ","").toLowerCase();
        }

        @Override
        public void run(AppConfiguration configuration, Environment environment) throws Exception {
            // get the proxy config so we can let the realm know where our host and port are.
            if (configuration.isAuthEnabled()) {
                ProxyConfiguration proxyConfig = configuration.getProxyConfiguration();

                HashSessionManager sm = new HashSessionManager();
                Map<String, String> env = System.getenv();
                String id = env.get("CONTROLPLANE_INSTANCE_ID");
                if (id == null) {
                    Random r = new Random();
                    id = String.valueOf(r.nextInt(65536));
                }
                //scope jsessionid cookie to have app name and instance id.
                //prevents session cookies being invalidated as they balance across instances
                sm.setSessionCookie(String.format("%s_%s_%s", sm.getSessionCookie(), this.name, id));
                SessionHandler sh = new SessionHandler(sm);
                sh.getSessionManager().setMaxInactiveInterval(configuration.getAuthTimeoutSeconds());
                environment.servlets().setSessionHandler(sh);


                // this allows individual zapps to specify a shiro.ini in their http section
                // i.e. http -> ContextParameter -> shiroConfigListeners
                // otherwise the default zauthbundle bundle shiro.ini is used.
                environment.servlets().addServletListeners(new EnvironmentLoaderListener());
                environment.servlets().addFilter("shiro-filter", new ShiroFilter()).
                        addMappingForUrlPatterns(null, false, URL_PATTERN);
                CloseableHttpClient httpClient = new HttpClientBuilder(environment).
                        using(configuration.getAuthHttpClientConfiguration()).build("auth-client");
                TokenRealm.init(proxyConfig, httpClient);
                TokenRealm.setAppName(this.name);
            }
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {
            // no setup here, everything is done in run
        }
    }
}
