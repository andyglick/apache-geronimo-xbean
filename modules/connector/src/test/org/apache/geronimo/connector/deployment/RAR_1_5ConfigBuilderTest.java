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

package org.apache.geronimo.connector.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.xmlbeans.XmlOptions;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.9 $ $Date: 2004/04/23 03:08:28 $
 *
 * */
public class RAR_1_5ConfigBuilderTest extends TestCase {
    private URL j2eeDD;
    private URL geronimoDD;
    XmlOptions xmlOptions;
    private List errors;

    private final Map gbeans = new HashMap();

    public void testLoadJ2eeDeploymentDescriptor() throws Exception {
        InputStream j2eeInputStream = j2eeDD.openStream();
        ConnectorDocument connectorDocument = ConnectorDocument.Factory.parse(j2eeInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }
    }

    public void testLoadGeronimoDeploymentDescriptor() throws Exception {
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument connectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }
    }

    public void testAddConnectorGBeans() throws Exception {
        InputStream j2eeInputStream = j2eeDD.openStream();
        ConnectorDocument connectorDocument = ConnectorDocument.Factory.parse(j2eeInputStream);
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument geronimoConnectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        File configStore = new File(System.getProperty("java.io.tmpdir"), "config-store");
        configStore.mkdir();
        Kernel kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        try {
            RAR_1_5ConfigBuilder configBuilder = new RAR_1_5ConfigBuilder(kernel, null, new ObjectName("geronimo.server:type=ConnectionTracker"));
            DeploymentContext context =  new MockDeploymentContext(kernel);
            configBuilder.addConnectorGBeans(context, connectorDocument, geronimoConnectorDocument.getConnector(), this.getClass().getClassLoader());
            for (Iterator iterator = gbeans.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                kernel.loadGBean((ObjectName)entry.getKey(), (GBeanMBean)entry.getValue());
            }
            for (Iterator iterator = gbeans.keySet().iterator(); iterator.hasNext();) {
                ObjectName name = (ObjectName) iterator.next();
                kernel.startRecursiveGBean(name);
            }
            for (Iterator iterator = gbeans.keySet().iterator(); iterator.hasNext();) {
                ObjectName name = (ObjectName) iterator.next();
                assertEquals("non started gbean: " + name, new Integer(State.RUNNING.toInt()), kernel.getAttribute(name, "state"));
            }
        } finally {
            kernel.shutdown();
        }
        assertEquals(15, gbeans.size());
        //we could check what the gbeans are...
    }

    public void testBuildConfig() throws Exception {
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument geronimoConnectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        File configStore = new File(System.getProperty("java.io.tmpdir"), "config-store");
        configStore.mkdir();
        File outfile = new File(System.getProperty("java.io.tmpdir"), "rar15outfile");
        Kernel kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        try {
            RAR_1_5ConfigBuilder configBuilder = new RAR_1_5ConfigBuilder(kernel, null, new ObjectName("geronimo.connector:service=ConnectionTracker"));
            configBuilder.buildConfiguration(outfile, null, getRARInputStream(), geronimoConnectorDocument);
        } finally {
            kernel.shutdown();
            outfile.delete();
        }
    }

    private InputStream getRARInputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarOutputStream jarOutputStream = new JarOutputStream(baos);
        ZipEntry entry = new ZipEntry("META-INF/ra.xml");
        jarOutputStream.putNextEntry(entry);

        InputStream j2eeInputStream = j2eeDD.openStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = j2eeInputStream.read(buffer)) > 0; ) {
            jarOutputStream.write(buffer, 0, length);
        }
        jarOutputStream.flush();
        jarOutputStream.closeEntry();
        jarOutputStream.close();

        return new ByteArrayInputStream(baos.toByteArray());
    }

    protected void setUp() throws Exception {
        File docDir = new File("src/test-data/connector_1_5");
        j2eeDD = new File(docDir, "ra.xml").toURL();
        geronimoDD = new File(docDir, "geronimo-ra.xml").toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }

    private class MockDeploymentContext extends DeploymentContext {

        MockDeploymentContext(Kernel kernel) throws DeploymentException, MalformedObjectNameException, IOException {
            super(null, null, null, kernel);
        }

        public void addGBean(ObjectName name, GBeanMBean gbean) {
            gbeans.put(name, gbean);
        }
    }

}
