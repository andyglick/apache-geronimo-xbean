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

package org.apache.geronimo.tomcat.cluster.wadi;

import org.apache.geronimo.clustering.wadi.WADISessionManager;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.tomcat.ObjectRetriever;


/**
 * 
 * @version $Rev$ $Date$
 */
public class WADIClusteredValveRetriever implements ObjectRetriever, GBeanLifecycle {
    private final WADISessionManager sessionManager;

    public WADIClusteredValveRetriever(WADISessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    
    public Object getInternalObject() {
        return new WADIClusteredValve(sessionManager);
    }

    public void doStart() throws Exception {
    }
    
    public void doStop() throws Exception {
    }
    
    public void doFail() {
    }
    
    public static final GBeanInfo GBEAN_INFO;
    public static final String GBEAN_REF_WADI_SESSION_MANAGER = "WADISessionManager";

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("WADI Clustered Valve Retriever",
            WADIClusteredValveRetriever.class,
            NameFactory.GERONIMO_SERVICE);

        infoFactory.addReference(GBEAN_REF_WADI_SESSION_MANAGER,
            WADISessionManager.class,
            NameFactory.GERONIMO_SERVICE);

        infoFactory.setConstructor(new String[]{GBEAN_REF_WADI_SESSION_MANAGER});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}