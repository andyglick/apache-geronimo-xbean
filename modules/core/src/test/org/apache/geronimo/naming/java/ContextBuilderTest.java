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
 *        Apache Software Foundation (http://www.apache.org/)."
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
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.naming.java;

import java.net.URL;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.management.ObjectName;

import junit.framework.TestCase;
import junit.framework.Test;
import org.apache.geronimo.deployment.model.geronimo.appclient.ApplicationClient;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.naming.jmx.TestObject;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2003/11/13 04:30:56 $
 */
public class ContextBuilderTest extends TestCase {
    private static final String objectName1 = "geronimo.test:name=test1";
    private static final String objectName2 = "geronimo.test:name=test2";

    private ApplicationClient client;
    private Context compCtx;
    private JMXKernel kernel;
    private ReferenceFactory referenceFactory;
    private TestObject testObject1 = new TestObject(new Object());
    private TestObject testObject2 = new TestObject(new Object());

    protected void setUp() throws Exception {
        kernel = new JMXKernel("geronimo.test");
        kernel.getMBeanServer().registerMBean(testObject1, ObjectName.getInstance(objectName1));
        kernel.getMBeanServer().registerMBean(testObject2, ObjectName.getInstance(objectName2));

        referenceFactory = new JMXReferenceFactory(kernel.getMBeanServerId());
        client = new ApplicationClient();
        EnvEntry stringEntry = new EnvEntry();
        stringEntry.setEnvEntryName("string");
        stringEntry.setEnvEntryType("java.lang.String");
        stringEntry.setEnvEntryValue("Hello World");
        EnvEntry intEntry = new EnvEntry();
        intEntry.setEnvEntryName("int");
        intEntry.setEnvEntryType("java.lang.Integer");
        intEntry.setEnvEntryValue("12345");

        EjbRef ejbRef = new EjbRef();
        ejbRef.setEJBRefName("here/there/EJB1");
        ejbRef.setEJBRefType("Home");
        ejbRef.setJndiName(objectName1);

        //EjbLocalRef ejbLocalRef = new EjbLocalRef();
        EjbRef ejbLocalRef = new EjbRef();
        ejbLocalRef.setEJBRefName("local/here/LocalEJB2");
        ejbLocalRef.setEJBRefType("LocalHome");
        ejbLocalRef.setJndiName(objectName2);

        ResourceRef urlRef = new ResourceRef();
        urlRef.setResRefName("url/testURL");
        urlRef.setResType(URL.class.getName());
        urlRef.setJndiName("http://localhost/path");

        ResourceRef cfRef = new ResourceRef();
        cfRef.setResRefName("DefaultCF");
        cfRef.setJndiName(objectName1);

        client.setEnvEntry(new EnvEntry[] { stringEntry, intEntry });
        //client.setEJBRef(new EjbRef[] {ejbRef});
        client.setEJBRef(new EjbRef[] {ejbRef, ejbLocalRef});
        //client.setEJBLocalRef(new EjbLocalRef[] {ejbLocalRef});


        client.setResourceRef(new ResourceRef[] { urlRef, cfRef });
    }

    public void testEnvEntries() throws Exception {
        compCtx = new ComponentContextBuilder(referenceFactory, null).buildContext(client);
        assertEquals("Hello World", compCtx.lookup("env/string"));
        assertEquals(new Integer(12345), compCtx.lookup("env/int"));
        assertEquals(new URL("http://localhost/path"), compCtx.lookup("env/url/testURL"));
    }

    public void testUserTransaction() throws Exception {
        compCtx = new ComponentContextBuilder(referenceFactory, null).buildContext(client);
        try {
            compCtx.lookup("UserTransaction");
            fail("Expected NameNotFoundException");
        } catch (NameNotFoundException e) {
            // OK
        }

        UserTransaction userTransaction = new UserTransactionImpl(null);
        compCtx = new ComponentContextBuilder(referenceFactory, userTransaction).buildContext(client);
        assertEquals(userTransaction, compCtx.lookup("UserTransaction"));
    }

    public void testEJBRefs() throws Exception {
        ReadOnlyContext compContext = new ComponentContextBuilder(referenceFactory, null).buildContext(client);
        RootContext.setComponentContext(compContext);
        InitialContext initialContext = new InitialContext();
        assertEquals("Expected object from testObject1", testObject1.getEJBHome(),
                initialContext.lookup("java:comp/env/here/there/EJB1"));
        assertEquals("Expected object from testObject2", testObject2.getEJBLocalHome(),
                initialContext.lookup("java:comp/env/local/here/LocalEJB2"));
        assertEquals("Expected object from testObject1", testObject1.getConnectionFactory(),
                initialContext.lookup("java:comp/env/DefaultCF"));
    }
}
