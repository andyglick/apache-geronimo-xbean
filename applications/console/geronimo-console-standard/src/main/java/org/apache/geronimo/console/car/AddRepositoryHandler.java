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
package org.apache.geronimo.console.car;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.system.plugin.PluginRepositoryList;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Handler for the import export main screen.
 *
 * @version $Rev: 409817 $ $Date: 2006-05-27 03:56:38 -0400 (Sat, 27 May 2006) $
 */
public class AddRepositoryHandler extends BaseImportExportHandler {
    private final static Log log = LogFactory.getLog(AddRepositoryHandler.class);

    public AddRepositoryHandler() {
        super(ADD_REPO_MODE, "/WEB-INF/view/car/addRepository.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        PluginRepositoryList[] lists = PortletManager.getCurrentServer(request).getPluginRepositoryLists();
        List list = new ArrayList();
        for (int i = 0; i < lists.length; i++) {
            PluginRepositoryList repo = lists[i];
            list.addAll(Arrays.asList(repo.getRepositories()));
        }
        String error = request.getParameter("repoError");
        if(error != null && !error.equals("")) {
            request.setAttribute("repoError", error);
        }
        request.setAttribute("repositories", list);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String repo = request.getParameter("newRepository");
        if(repo != null && !repo.equals("")) {
            if(!addRepository(repo, request, response)) {
                return getMode();
            }
        }
        return INDEX_MODE+BEFORE_ACTION;
    }


    private boolean addRepository(String repo, ActionRequest request, ActionResponse response) throws IOException {
        if(!repo.endsWith("/")) {
            repo = repo+"/";
        }
        PluginRepositoryList[] lists = PortletManager.getCurrentServer(request).getPluginRepositoryLists();

        // Check for duplicates
        for (int i = 0; i < lists.length; i++) {
            PluginRepositoryList test = lists[i];
            URL[] all = test.getRepositories();
            for (int j = 0; j < all.length; j++) {
                String existing = all[j].toString();
                if(!existing.endsWith("/")) {
                    existing = existing + "/";
                }
                if(repo.equals(existing)) {
                    response.setRenderParameter("repoError", "Already have an entry for repository "+repo);
                    return false;
                }
            }
        }

        // Verify the repository and add it if valid
        if(lists.length > 0) {
            URL url;
            try {
                url = new URL(repo);
            } catch (MalformedURLException e) {
                response.setRenderParameter("repoError", "Invalid repository URL "+repo);
                return false;
            }
            URL test = new URL(repo+"geronimo-plugins.xml");
            log.debug("Checking repository "+test);
            URLConnection urlConnection = test.openConnection();
            if(urlConnection instanceof HttpURLConnection) {
                HttpURLConnection con = (HttpURLConnection) urlConnection;
                try {
                    con.connect();
                } catch (ConnectException e) {
                    response.setRenderParameter("repoError", "Unable to connect to "+url+" ("+e.getMessage()+")");
                    return false;
                }
                int result = con.getResponseCode();
                log.debug("Repository check response: "+result);
                if(result == 404) {
                    response.setRenderParameter("repoError", "Not a valid repository; no plugin list found at "+test);
                    return false;
                } else if(result == 401) {
                    log.warn("Unable to validate repository -- it requires authentication.  Assuming you know what you're doing.");
                } else if(result != 200) {
                    log.warn("Unexpected response code while validating repository ("+result+" "+con.getResponseMessage()+").  Assuming you know what you're doing.");
                }
                con.disconnect();
            } else {
                try {
                    urlConnection.connect();
                    InputStream in = urlConnection.getInputStream();
                    in.read();
                    in.close();
                } catch (IOException e) {
                    response.setRenderParameter("repoError", "Not a valid repository; no plugin list found at "+test);
                    return false;
                }
            }
            lists[0].addUserRepository(url);
            request.setAttribute("repository", repo);
            return true;
        }
        response.setRenderParameter("repoError", "No repository list found; unable to store new repository");
        return false;
    }
}
