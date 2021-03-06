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

package org.apache.geronimo.deployment;

import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.model.DeployableObject;

/**
 * Interface to be implemented by the deployment subsystem for any type
 * of component (e.g. EAR, WAR, ...) that needs configuration.
 * 
 * @version $Rev$ $Date$
 */
public interface ModuleConfigurer {
    /**
     * JSR88 method for getting the vendor part of the deployment configuration
     * @param deployable the object the tool is trying to deploy
     * @return the vendor-specific deployment configuration, or null a configurer cannot handle the DeployableObject
     */
    DeploymentConfiguration createConfiguration(DeployableObject deployable);
}
