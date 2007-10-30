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
package org.apache.geronimo.tomcat;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * @version $Rev$ $Date$
 */
public class ApplicationTest extends AbstractWebModuleTest {
    private File basedir = new File(System.getProperty("basedir"));
    
    public void testApplication() throws Exception {
        setUpInsecureAppContext(new File(basedir, "target/var/catalina/webapps/war1/").toURI(),
                new File(basedir, "target/var/catalina/webapps/war1/WEB-INF/web.xml").toURL(),
                null,
                null,
                null,
                null);

        HttpURLConnection connection = (HttpURLConnection) new URL(connector.getConnectUrl() +  "/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
    }

    protected void setUp() throws Exception {
        super.setUp();
        super.init(null);
    }
}
