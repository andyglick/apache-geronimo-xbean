/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.Kernel;

/**
 * Implementation of ConfigurationStore that loads Configurations from a repository.
 * This implementation is read-only on the assumption that a separate maven task will
 * handle installation of a built package into the repository.
 *
 * @version $Rev$ $Date$
 */
public class MavenConfigStore implements ConfigurationStore {
    private final Kernel kernel;
    private final ObjectName objectN ame;
    private final Repository repository;
    private final ManageableAttributeStore attributeStore;

    public MavenConfigStore(Kernel kernel, String objectName, Repository repository, ManageableAttributeStore attributeStore) throws MalformedObjectNameException {
        this.kernel = kernel;
        this.objectName = new ObjectName(objectName);
        this.repository = repository;
        this.attributeStore = attributeStore;
    }

    public String getObjectName() {
        return objectName.toString();
    }

    public synchronized ObjectName loadConfiguration(URI configId) throws NoSuchConfigException, IOException, InvalidConfigException {
        if (!repository.hasURI(configId)) {
            throw new NoSuchConfigException("Configuration not found: " + configId);
        }

        GBeanData config = new GBeanData();
        URL baseURL = new URL("jar:" + repository.getURL(configId).toString() + "!/");
        InputStream jis = null;
        try {
            URL stateURL = new URL(baseURL, "META-INF/config.ser");
            jis = stateURL.openStream();
            ObjectInputStream ois = new ObjectInputStream(jis);
            config.readExternal(ois);
            config.setReferencePattern("ConfigurationStore", objectName);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigException("Unable to load class from config: " + configId, e);
        } finally {
            if (jis != null) {
                jis.close();
            }
        }

        ObjectName name;
        try {
            name = Configuration.getConfigurationObjectName(configId);
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Cannot convert id to ObjectName: ", e);
        }
        config.setName(name);
        ObjectName pattern;
        try {
            pattern = attributeStore == null ? null : new ObjectName(attributeStore.getObjectName());
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException("Invalid ObjectName for AttributeStore: " + attributeStore.getObjectName());
        }
        config.setReferencePattern("AttributeStore", pattern);

        try {
            kernel.loadGBean(config, Configuration.class.getClassLoader());
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to register configuration", e);
        }

        try {
            kernel.setAttribute(name, "baseURL", baseURL);
        } catch (Exception e) {
            try {
                kernel.unloadGBean(name);
            } catch (Exception ignored) {
                // ignore
            }
            throw new InvalidConfigException("Cannot set baseURL", e);
        }

        return name;
    }

    public boolean containsConfiguration(URI configID) {
        return repository.hasURI(configID);
    }

    public File createNewConfigurationDir() {
        try {
            File tmpFile = File.createTempFile("package", ".tmpdir");
            tmpFile.delete();
            tmpFile.mkdir();
            if (!tmpFile.isDirectory()) {
                return null;
            }
            return tmpFile;
        } catch (IOException e) {
            // doh why can't I throw this?
            return null;
        }
    }

    public URI install(URL source) throws IOException, InvalidConfigException {
        throw new UnsupportedOperationException();
    }

    public void install(ConfigurationData configurationData, File source) throws IOException, InvalidConfigException {
        throw new UnsupportedOperationException();
    }

    public void uninstall(URI configID) throws NoSuchConfigException, IOException {
        throw new UnsupportedOperationException();
    }

    public void updateConfiguration(ConfigurationData configurationData) throws NoSuchConfigException, Exception {
        // we don't store persistent state
    }

    public List listConfiguations() {
        throw new UnsupportedOperationException();
    }


    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = new GBeanInfoBuilder(MavenConfigStore.class);
        builder.addInterface(ConfigurationStore.class);
        builder.addAttribute("kernel", Kernel.class, false);
        builder.addAttribute("objectName", String.class, false);
        builder.addReference("Repository", Repository.class);
        builder.addReference("AttributeStore", ManageableAttributeStore.class);
        builder.setConstructor(new String[]{"kernel", "objectName", "Repository", "AttributeStore"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
