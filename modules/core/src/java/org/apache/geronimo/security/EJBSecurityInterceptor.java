/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security;

import org.apache.geronimo.core.service.AbstractInterceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.metadata.EJBMetadata;
import org.apache.geronimo.ejb.metadata.MethodMetadata;
import org.apache.geronimo.security.util.ContextManager;

import javax.security.jacc.PolicyContext;
import javax.security.auth.Subject;
import javax.ejb.EJBException;
import java.lang.reflect.Method;
import java.security.AccessControlContext;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/12 04:31:55 $
 */
public class EJBSecurityInterceptor extends AbstractInterceptor {
    private Subject runAsSubject;
    private EJBMetadata ejbMetadata;
    private GeronimoPolicyConfiguration policyConfiguration;

    public Subject getRunAsSubject() {
        return runAsSubject;
    }

    public void setRunAsSubject(Subject runAsSubject) {
        this.runAsSubject = runAsSubject;
    }

    public EJBMetadata getEjbMetadata() {
        return ejbMetadata;
    }

    public void setEjbMetadata(EJBMetadata ejbMetadata) {
        this.ejbMetadata = ejbMetadata;
    }

    public GeronimoPolicyConfiguration getPolicyConfiguration() {
        return policyConfiguration;
    }

    public void setPolicyConfiguration(GeronimoPolicyConfiguration policyConfiguration) {
        this.policyConfiguration = policyConfiguration;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {

        AccessControlContext context;
        if (runAsSubject != null) {
            ContextManager.pushSubject(runAsSubject);
        }
        context = ContextManager.peekContext();

        String savedContextId = PolicyContext.getContextID();

        InvocationResult result = null;
        try {
            Method method = EJBInvocationUtil.getMethod(invocation);
            EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(getContainer());
            MethodMetadata methodMetadata = ejbMetadata.getMethodMetadata(method);

            // Optimization: Why go on if this method has been excluded?
            if (methodMetadata.isExcluded()) {
                throw new EJBException("Calls to this method have been excluded");
            }

            // Communicate the context id to the Policy provider
            PolicyContext.setContextID(ejbMetadata.getPolicyContextId());

            if (context != null) context.checkPermission(methodMetadata.getEJBMethodPermission());

            result = getNext().invoke(invocation);
        } finally {
            PolicyContext.setContextID(savedContextId);

            if (runAsSubject != null) ContextManager.popSubject();
        }
        return result;
    }
}
