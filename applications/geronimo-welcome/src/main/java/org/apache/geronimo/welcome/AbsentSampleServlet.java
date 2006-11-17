/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.welcome;

import java.io.IOException;
import java.net.URL;
import java.util.Set;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.PluginInstaller;
import org.apache.geronimo.system.plugin.PluginList;
import org.apache.geronimo.system.plugin.PluginMetadata;
import org.apache.geronimo.system.plugin.PluginRepositoryList;
import org.apache.geronimo.system.plugin.DownloadResults;

/**
 * Stands in for servlets that are not yet installed, offering to install them.
 *
 * @version $Rev$ $Date$
 */
public class AbsentSampleServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String install = request.getParameter("install");
        if(install != null && !install.equals("")) {
            doInstall(request, response);
        } else {
            doMessage(request, response);
        }
    }

    private void doMessage(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/sampleNotInstalled.jsp");
        dispatcher.forward(request, response);
    }

    private void doInstall(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Kernel kernel = KernelRegistry.getSingleKernel();
        PluginInstaller installer = getPluginInstaller(kernel);
        String moduleIdName = getInitParameter("moduleId");
        moduleIdName = moduleIdName.replaceAll("SERVER", getServerType());
        URL repo = getFirstPluginRepository(kernel);
        PluginMetadata target = new PluginMetadata("Sample Application", null, "Samples", "A sample application",
                                                   null, null, null, false, true);
        target.setDependencies(new String[]{moduleIdName});
        DownloadResults results = installer.install(new PluginList(new URL[]{repo, new URL("http://www.ibiblio.org/maven2/")},
                                                    new PluginMetadata[]{target}), null, null);
        if(results.isFailed()) {
            throw new ServletException("Unable to install sample application", results.getFailure());
        }
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
        for (int i = 0; i < results.getInstalledConfigIDs().length; i++) {
            Artifact artifact = results.getInstalledConfigIDs()[i];
            if(mgr.isConfiguration(artifact)) {
                try {
                    if(!mgr.isLoaded(artifact)) {
                        mgr.loadConfiguration(artifact);
                    }
                    if(!mgr.isRunning(artifact)) {
                        mgr.startConfiguration(artifact);
                    }
                } catch (NoSuchConfigException e) {
                    throw new ServletException("Unable to start sample application", e);
                } catch (LifecycleException e) {
                    throw new ServletException("Unable to start sample application", e);
                }
            }
        }
        response.sendRedirect(request.getContextPath()+request.getServletPath()+"/");
    }

    private String getServerType() {
        return getServletContext().getServerInfo().toLowerCase().indexOf("jetty") > -1 ? "jetty" : "tomcat";
    }

    private PluginInstaller getPluginInstaller(Kernel kernel) throws ServletException {
        Set installers = kernel.listGBeans(new AbstractNameQuery(PluginInstaller.class.getName()));
        if(installers.size() == 0) {
            throw new ServletException("Unable to install sample application; no plugin installer found");
        }
       return (PluginInstaller)kernel.getProxyManager().createProxy((AbstractName) installers.iterator().next(),
                                                                    PluginInstaller.class);
    }

    private URL getFirstPluginRepository(Kernel kernel) throws ServletException {
        Set installers = kernel.listGBeans(new AbstractNameQuery(PluginRepositoryList.class.getName()));
        if(installers.size() == 0) {
            throw new ServletException("Unable to install sample application; no plugin repository list found");
        }
        PluginRepositoryList repos = ((PluginRepositoryList) kernel.getProxyManager().createProxy((AbstractName) installers.iterator().next(),
                PluginRepositoryList.class));

        URL[] urls = repos.getRepositories();
        if(urls.length == 0) {
            repos.refresh();
            urls = repos.getRepositories();
            if(urls.length == 0) {
                throw new ServletException("Unable to install sample applicatoin; unable to download repository list");
            }
        }
        return urls[0];
    }
}
