/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.plugin.local;

import java.io.File;
import java.io.InputStream;
import javax.enterprise.deploy.shared.CommandType;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.10 $ $Date: 2004/04/23 03:08:28 $
 */
public class DistributeCommand extends CommandSupport {
    private final ConfigurationStore store;
    private final ConfigurationBuilder builder;
    private final InputStream in;
    private final XmlObject plan;

    public DistributeCommand(ConfigurationStore store, ConfigurationBuilder builder, InputStream in, XmlObject plan) {
        super(CommandType.DISTRIBUTE);
        this.store = store;
        this.builder = builder;
        this.in = in;
        this.plan = plan;
    }

    public void run() {
        File configFile = null;
        try {
            // create some working space
            configFile = File.createTempFile("deploy", ".car");
            builder.buildConfiguration(configFile, null, in, plan);

            // install in our local server
            store.install(configFile.toURL());
            //addModule(targetID);
            complete("Completed");
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (configFile != null) {
                configFile.delete();
            }
        }
    }
}
