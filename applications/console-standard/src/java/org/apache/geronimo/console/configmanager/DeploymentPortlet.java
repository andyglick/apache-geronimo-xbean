/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.configmanager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;

public class DeploymentPortlet extends BasePortlet {
    private final String LINE_SEP = System.getProperty("line.separator");

    private PortletRequestDispatcher deployView;

    private PortletRequestDispatcher helpView;

    private Kernel kernel;

    private static final String[] ARGS = {File.class.getName(),
                                          File.class.getName()};

    private boolean messageNotRendered = true;

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        messageNotRendered = true;
        if (!PortletFileUpload.isMultipartContent(actionRequest)) {
            throw new PortletException("Expected file upload");
        }

        File rootDir = new File(System.getProperty("java.io.tmpdir"));
        PortletFileUpload uploader = new PortletFileUpload(new DiskFileItemFactory(10240, rootDir));
        File moduleFile = null;
        File planFile = null;
        String startApp = null;
        try {
            List items = uploader.parseRequest(actionRequest);
            for (Iterator i = items.iterator(); i.hasNext();) {
                FileItem item = (FileItem) i.next();
                if (!item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String name = item.getName().trim();
                    File file;
                    if (name.length() == 0) {
                        file = null;
                    } else {
                        // Firefox sends basename, IE sends full path
                        int index = name.lastIndexOf('\\');
                        if (index != -1) {
                            name = name.substring(index + 1);
                        }
                        file = new File(rootDir, name);
                    }
                    if ("module".equals(fieldName)) {
                        moduleFile = file;
                    } else if ("plan".equals(fieldName)) {
                        planFile = file;
                    }
                    if (file != null) {
                        try {
                            item.write(file);
                        } catch (Exception e) {
                            throw new PortletException(e);
                        }
                    }
                } else {
                    // retrieve 'startApp' form field value
                    if ("startApp".equalsIgnoreCase(item.getFieldName())) {
                        startApp = item.getString();
                    }
                }
            }
        } catch (FileUploadException e) {
            throw new PortletException(e);
        }
        try {
            List list = (List) kernel.invoke(ObjectNameConstants.DEPLOYER_OBJECT_NAME, "deploy", new Object[]{
                moduleFile, planFile}, ARGS);
            actionResponse.setRenderParameter("outcome",
                    "The application was successfully deployed.<br/>");
            // start installed app/s
            if ((startApp != null) && "YES".equalsIgnoreCase(startApp)) {
                ConfigurationManager configurationManager = ConfigurationUtil
                        .getConfigurationManager(kernel);
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    URI configID = URI.create((String)iterator.next());
                    if (!configurationManager.isLoaded(configID)) {
                        configurationManager.load(configID);
                    }
                    configurationManager.loadGBeans(configID);
                    configurationManager.start(configID);
                }
            }
        } catch (DeploymentException e) {
            e.printStackTrace();
            StringBuffer buf = new StringBuffer(256);
            Throwable cause = e;
            while (cause != null) {
                append(buf, cause.getMessage());
                buf.append(LINE_SEP);
                cause = cause.getCause();
            }
            actionResponse.setRenderParameter("outcome", buf.toString());
        } catch (Exception e) {
            throw new PortletException(e);
        }
    }

    private void append(StringBuffer buf, String message) {
        for (int i = 0; i < message.length(); i++) {
            char ch = message.charAt(i);
            if (ch == '<') {
                buf.append("&lt;");
            } else if (ch == '>') {
                buf.append("&gt;");
            } else {
                buf.append(ch);
            }
        }
    }

    protected void doView(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        if (messageNotRendered) {
            renderRequest.setAttribute("outcome", renderRequest
                    .getParameter("outcome"));
            messageNotRendered = false;
        }
        deployView.include(renderRequest, renderResponse);
        // clear previous message for next rendering
    }

    protected void doHelp(RenderRequest renderRequest,
                          RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        deployView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/deploy.jsp");
        helpView = portletConfig.getPortletContext().getRequestDispatcher("/WEB-INF/view/configmanager/deployHelp.jsp");
    }

    public void destroy() {
        deployView = null;
        helpView = null;
        kernel = null;
        super.destroy();
    }
}
