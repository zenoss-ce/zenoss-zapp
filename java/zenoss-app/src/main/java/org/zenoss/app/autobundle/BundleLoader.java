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
import org.glassfish.jersey.server.ResourceFinder;
import org.glassfish.jersey.server.internal.scanning.AnnotationAcceptingListener;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.zenoss.app.annotations.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;


/**
 * Scan packages for classes annotated with {@link Bundle} and load them into Dropwizard
 */
public final class BundleLoader {

    public void loadBundles(Bootstrap bootstrap, Class c, String... packages) throws Exception {
        for (Class clz : this.findBundles(packages)) {
            Object o = clz.newInstance();
            registerBundle(o, bootstrap, c);
        }
    }

    Set<Class<?>> findBundles(String... packages) throws IOException {
        final AnnotationAcceptingListener aal = new AnnotationAcceptingListener(Bundle.class);
        ResourceFinder rf = new PackageNamesScanner(packages, true);
        while (rf.hasNext()) {
            final String next = rf.next();
            if (aal.accept(next)) {
                final InputStream in = rf.open();
                aal.process(next, in);
                in.close();
            }
        }
        return aal.getAnnotatedClasses();
    }

    void registerBundle(Object o, Bootstrap bootstrap, Class c) {
        if (o instanceof AutoBundle) {
            AutoBundle ab = (AutoBundle) o;
            checkConfigType(c, ab);
            bootstrap.addBundle(ab.getBundle());

        } else if (o instanceof AutoConfiguredBundle) {
            AutoConfiguredBundle ab = (AutoConfiguredBundle) o;
            checkConfigType(c, ab);
            bootstrap.addBundle(ab.getBundle(bootstrap));

        } else if (o instanceof ConfiguredBundle) {
            bootstrap.addBundle((ConfiguredBundle) o);
        } else if (o instanceof io.dropwizard.Bundle) {
            bootstrap.addBundle((io.dropwizard.Bundle) o);
        } else {
            throw new UnknownBundle("Unknown bundle type " + o.getClass().getName());
        }
    }

    private void checkConfigType(Class c, AutoBundle ab) {
        checkConfigAssignment(c, ab.getRequiredConfig());
    }


    private void checkConfigType(Class c, AutoConfiguredBundle ab) {
        checkConfigAssignment(c, ab.getRequiredConfig());
    }

    private void checkConfigAssignment(Class c, Optional<Class> required) {
        if (required.isPresent() && !required.get().isAssignableFrom(c)) {
            throw new BundleLoadException("Configuration " + c.getName() + " does not implement required " + required.get());
        }
    }

}
