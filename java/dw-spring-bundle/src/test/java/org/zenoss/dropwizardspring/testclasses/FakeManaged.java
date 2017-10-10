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


package org.zenoss.dropwizardspring.testclasses;

import io.dropwizard.lifecycle.Managed;

@org.zenoss.dropwizardspring.annotations.Managed
public class FakeManaged implements Managed {

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }
}
