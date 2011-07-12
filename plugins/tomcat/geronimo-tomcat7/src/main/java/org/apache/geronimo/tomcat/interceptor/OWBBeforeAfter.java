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


package org.apache.geronimo.tomcat.interceptor;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.openejb.cdi.GeronimoSingletonService;
import org.apache.webbeans.config.WebBeansContext;

/**
 * @version $Rev$ $Date$
 */
public class OWBBeforeAfter implements BeforeAfter {

    private final BeforeAfter next;
    private final int index;
    private final WebBeansContext owbContext ;

    public OWBBeforeAfter(BeforeAfter next, int index, ServletContext servletContext, WebBeansContext owbContext) {
        this.next = next;
        this.index = index;
        this.owbContext = owbContext;
    }

    @Override
    public void before(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        try {
            beforeAfterContext.contexts[index] = GeronimoSingletonService.contextEntered(owbContext);
            beforeAfterContext.clearRequiredFlags[index] = true;
            if (next != null) {
                next.before(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
        } catch (RuntimeException e) {
            GeronimoSingletonService.contextExited((WebBeansContext) beforeAfterContext.contexts[index]);
            beforeAfterContext.clearRequiredFlags[index] = false;
            throw e;
        }
    }

    @Override
    public void after(BeforeAfterContext beforeAfterContext, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        try {
            if (next != null) {
                next.after(beforeAfterContext, httpRequest, httpResponse, dispatch);
            }
        } finally {
            if (beforeAfterContext.clearRequiredFlags[index]) {
                GeronimoSingletonService.contextExited((WebBeansContext) beforeAfterContext.contexts[index]);
                beforeAfterContext.clearRequiredFlags[index] = false;
            }
        }
    }
}
