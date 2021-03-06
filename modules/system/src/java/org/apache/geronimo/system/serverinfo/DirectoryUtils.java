/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.system.serverinfo;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public final class DirectoryUtils {
    private static final Log log = LogFactory.getLog(DirectoryUtils.class);
    private static final File geronimoInstallDirectory;

    static {
        // guess from the location of the jar
        URL url = DirectoryUtils.class.getClassLoader().getResource("META-INF/startup-jar");

        File directory = null;
        if (url != null) {
            try {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                url = jarConnection.getJarFileURL();

                URI baseURI = new URI(url.toString()).resolve("..");
                directory = new File(baseURI);
            } catch (Exception ignored) {
                log.error("Error while determining the geronimo installation directory", ignored);
            }
        } else {
            log.error("Cound not determin the geronimo installation directory, because the startup jar could not be found in the current class loader.");
        }
        geronimoInstallDirectory = directory;
    }

    public static File getGeronimoInstallDirectory() {
        return geronimoInstallDirectory;
    }
}
