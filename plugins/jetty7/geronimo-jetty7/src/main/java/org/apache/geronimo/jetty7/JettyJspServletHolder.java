/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.jetty7;

import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.SERVLET_TEMPLATE)
public class JettyJspServletHolder extends JettyServletHolder {

    public JettyJspServletHolder() {
    }

    public JettyJspServletHolder(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                                 @ParamAttribute(name = "servletName") String servletName,
                                 @ParamAttribute(name = "servletClass") String servletClassName,
                                 @ParamAttribute(name = "jspFile") String jspFile,
                                 @ParamAttribute(name = "initParams") Map initParams,
                                 @ParamAttribute(name = "loadOnStartup") Integer loadOnStartup,
                                 @ParamAttribute(name = "servletMappings") Set<String> servletMappings,
                                 @ParamAttribute(name = "runAsRole") String runAsRole,
                                 @ParamReference(name = "JettyServletRegistration", namingType = NameFactory.WEB_MODULE) JettyServletRegistration context) throws Exception {
        super(objectName, servletName, servletClassName, jspFile, initParams, loadOnStartup, servletMappings, runAsRole, context);
    }

}