/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.mavenplugins.geronimo;

import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.geronimo.genesis.AntMojoSupport;

/**
 * Support for Geronimo server mojos.
 *
 * @version $Rev$ $Date$
 */
public abstract class ServerMojoSupport
    extends AntMojoSupport
{
    static {
        //
        // HACK: Since this module needs to pick up some classes from geronimo-kernel, and since
        //       that module assumes that you want to use its logging, we have to forcefully
        //       configure JCL to use its defaults, and tell the Geronomo logging not to bootstrap iniitalize.
        //
        
        System.setProperty("org.apache.commons.logging.LogFactory", "org.apache.commons.logging.impl.LogFactoryImpl");
        System.setProperty("geronimo.bootstrap.logging.enabled", "false");
    }
    
    /**
     * The port number to connect to the server..
     *
     * @parameter expression="${port}" default-value="1099"
     */
    protected int port = -1;

    /**
     * The username to authenticate with.
     *
     * @parameter expression="${username}" default-value="system"
     */
    protected String username = null;

    /**
     * The password to authenticate with.
     *
     * @parameter expression="${password}" default-value="manager"
     */
    protected String password = null;

    //
    // MojoSupport Hooks
    //

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project = null;

    protected MavenProject getProject() {
        return project;
    }

    protected void init() throws MojoExecutionException, MojoFailureException {
        super.init();

        // Install the bridge from JCL to this plugins Log
        MavenPluginLog.setLog(log);
    }
}
