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
package org.apache.geronimo.system.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Stores managed attributes in an XML file on the local filesystem.
 *
 * @version $Rev$ $Date$
 */
public class LocalAttributeManager implements PluginAttributeStore, PersistentConfigurationList, GBeanLifecycle {
    private final static Log log = LogFactory.getLog(LocalAttributeManager.class);

    private final static String CONFIG_FILE_PROPERTY = "org.apache.geronimo.config.file";

    private final static String BACKUP_EXTENSION = ".bak";
    private final static String TEMP_EXTENSION = ".working";
    private final static int SAVE_BUFFER_MS = 5000;

    private final ServerInfo serverInfo;
    private final String configFile;
    private final boolean readOnly;

    private File attributeFile;
    private File backupFile;
    private File tempFile;
    private ServerOverride serverOverride;

    private Timer timer;
    private TimerTask currentTask;

    private boolean kernelFullyStarted;

    public LocalAttributeManager(String configFile, boolean readOnly, ServerInfo serverInfo) {
        this.configFile = System.getProperty(CONFIG_FILE_PROPERTY, configFile);
        this.readOnly = readOnly;
        this.serverInfo = serverInfo;
        serverOverride = new ServerOverride();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public synchronized Collection applyOverrides(Artifact configName, Collection gbeanDatas, ClassLoader classLoader) throws InvalidConfigException {
        // clone the datas since we will be modifying this collection
        gbeanDatas = new ArrayList(gbeanDatas);

        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return gbeanDatas;
        }

        // index the incoming datas
        Map datasByName = new HashMap();
        for (Iterator iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            datasByName.put(gbeanData.getAbstractName(), gbeanData);
            datasByName.put(gbeanData.getAbstractName().getName().get("name"), gbeanData);
        }

        // add the new GBeans
        for (Iterator iterator = configuration.getGBeans().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object name = entry.getKey();
            GBeanOverride gbean = (GBeanOverride) entry.getValue();
            if (!datasByName.containsKey(name) && gbean.isLoad()) {
                if (gbean.getGBeanInfo() == null || !(name instanceof AbstractName)) {
                    String sep = "";
                    StringBuffer message = new StringBuffer("New GBeans must be specified with ");
                    if (gbean.getGBeanInfo() == null) {
                        message.append("a GBeanInfo ");
                        sep = "and ";
                    }
                    if (!(name instanceof AbstractName)) {
                        message.append(sep).append("a full AbstractName ");
                    }
                    message.append("configuration=").append(configName);
                    message.append(" gbeanName=").append(name);
                    throw new InvalidConfigException(message.toString());
                }
                GBeanInfo gbeanInfo = GBeanInfo.getGBeanInfo(gbean.getGBeanInfo(), classLoader);
                AbstractName abstractName = (AbstractName)name;
                GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
                gbeanDatas.add(gBeanData);
            }
        }

        // set the attributes
        for (Iterator iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData data = (GBeanData) iterator.next();
            boolean load = setAttributes(data, configuration, configName, classLoader);
            if (!load) {
                iterator.remove();
            }
        }
        return gbeanDatas;
    }

    /**
     * Set the attributes from the attribute store on a single gbean, and return whether or not to load the gbean.
     *
     * @param data
     * @param configuration
     * @param configName
     * @param classLoader
     * @return true if the gbean should be loaded, false otherwise.
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     *
     */
    private synchronized boolean setAttributes(GBeanData data, ConfigurationOverride configuration, Artifact configName, ClassLoader classLoader) throws InvalidConfigException {
        AbstractName gbeanName = data.getAbstractName();
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
        }

        if (gbean == null) {
            //no attr info, load by default
            return true;
        }

        if (!gbean.isLoad()) {
            return false;
        }

        GBeanInfo gbeanInfo = data.getGBeanInfo();

        // set attributes
        for (Iterator iterator = gbean.getAttributes().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String attributeName = (String) entry.getKey();
            GAttributeInfo attributeInfo = gbeanInfo.getAttribute(attributeName);
            if (attributeInfo == null) {
                throw new InvalidConfigException("No attribute: " + attributeName + " for gbean: " + data.getAbstractName());
            }
            String valueString = (String) entry.getValue();
            Object value = getValue(attributeInfo, valueString, configName, gbeanName, classLoader);
            data.setAttribute(attributeName, value);
        }

        //Clear attributes
        for (Iterator iterator = gbean.getClearAttributes().iterator(); iterator.hasNext();){
           String attribute = (String) iterator.next();
           if (gbean.getClearAttribute(attribute)){
               data.clearAttribute(attribute);
           }
        }

        //Null attributes
        for (Iterator iterator = gbean.getNullAttributes().iterator(); iterator.hasNext();){
           String attribute = (String) iterator.next();
           if (gbean.getNullAttribute(attribute)){
               data.setAttribute(attribute, null);
           }
        }

        // set references
        for (Iterator iterator = gbean.getReferences().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();

            String referenceName = (String) entry.getKey();
            GReferenceInfo referenceInfo = gbeanInfo.getReference(referenceName);
            if (referenceInfo == null) {
                throw new InvalidConfigException("No reference: " + referenceName + " for gbean: " + data.getAbstractName());
            }

            ReferencePatterns referencePatterns = (ReferencePatterns) entry.getValue();

            data.setReferencePatterns(referenceName, referencePatterns);
        }

        //Clear references
        for (Iterator iterator = gbean.getClearReferences().iterator(); iterator.hasNext();){
           String reference = (String) iterator.next();
           if (gbean.getClearReference(reference)){
               data.clearReference(reference);
           }
        }

        return true;
    }


    private synchronized Object getValue(GAttributeInfo attribute, String value, Artifact configurationName, AbstractName gbeanName, ClassLoader classLoader) {
        if (value == null) {
            return null;
        }

        try {
            PropertyEditor editor = PropertyEditors.findEditor(attribute.getType(), classLoader);
            if (editor == null) {
                log.debug("Unable to parse attribute of type " + attribute.getType() + "; no editor found");
                return null;
            }
            editor.setAsText(value);
            log.debug("Setting value for " + configurationName + "/" + gbeanName + "/" + attribute.getName() + " to value " + value);
            return editor.getValue();
        } catch (ClassNotFoundException e) {
            log.error("Unable to load attribute type " + attribute.getType());
            return null;
        }
    }

    public void setModuleGBeans(Artifact moduleName, GBeanOverride[] gbeans) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(moduleName, true);
        for (int i = 0; i < gbeans.length; i++) {
            GBeanOverride gbean = gbeans[i];
            configuration.addGBean(gbean);
        }
        attributeChanged();
    }

    public synchronized void setValue(Artifact configurationName, AbstractName gbeanName, GAttributeInfo attribute, Object value) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
            if (gbean == null) {
                gbean = new GBeanOverride(gbeanName, true);
                configuration.addGBean(gbeanName, gbean);
            }
        }

        try {
            gbean.setAttribute(attribute.getName(), value, attribute.getType());
            attributeChanged();
        } catch (InvalidAttributeException e) {
            // attribute can not be represented as a string
            log.error(e.getMessage());
        }
    }

    public synchronized void setReferencePatterns(Artifact configurationName, AbstractName gbeanName, GReferenceInfo reference, ReferencePatterns patterns) {
        if (readOnly) {
            return;
        }

        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String)gbeanName.getName().get("name"));
            if (gbean == null) {
                gbean = new GBeanOverride(gbeanName, true);
                configuration.addGBean(gbeanName, gbean);
            }
        }
        gbean.setReferencePatterns(reference.getName(), patterns);
        attributeChanged();
    }

    public synchronized void setShouldLoad(Artifact configurationName, AbstractName gbeanName, boolean load) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);

        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            // attempt to lookup by short name
            gbean = configuration.getGBean((String)gbeanName.getName().get("name"));
        }

        if (gbean == null) {
            gbean = new GBeanOverride(gbeanName, load);
            configuration.addGBean(gbeanName, gbean);
        } else {
            gbean.setLoad(load);
        }
        attributeChanged();
    }

    public void addGBean(Artifact configurationName, GBeanData gbeanData) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName);
        if (configuration == null) {
            log.debug("Can not add GBean; Configuration not found " + configurationName);
            return;
        }
        try {
            GBeanOverride gbean = new GBeanOverride(gbeanData);
            configuration.addGBean(gbean);
            attributeChanged();
        } catch (InvalidAttributeException e) {
            // attribute can not be represented as a string
            log.error(e.getMessage());
        }
    }

    public synchronized void load() throws IOException {
        ensureParentDirectory();
        if (!attributeFile.exists()) {
            return;
        }
        FileInputStream fis = new FileInputStream(attributeFile);
        InputSource in = new InputSource(fis);
        in.setSystemId(attributeFile.toString());
        DocumentBuilderFactory dFactory = XmlUtil.newDocumentBuilderFactory();
        try {
            dFactory.setValidating(true);
            dFactory.setNamespaceAware(true);
            dFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                                 "http://www.w3.org/2001/XMLSchema");
            dFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                                 LocalAttributeManager.class.getResourceAsStream("/META-INF/schema/local-attributes-1.1.xsd"));

            DocumentBuilder builder = dFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException exception) {
                    log.error("Unable to read saved manageable attributes. " +
                        "SAX parse error: " + exception.getMessage() +
                        " at line " + exception.getLineNumber() +
                        ", column " + exception.getColumnNumber() +
                        " in entity " + exception.getSystemId());
                    // TODO throw an exception here?
                }

                public void fatalError(SAXParseException exception) {
                    log.error("Unable to read saved manageable attributes. " +
                            "Fatal SAX parse error: " + exception.getMessage() +
                            " at line " + exception.getLineNumber() +
                            ", column " + exception.getColumnNumber() +
                            " in entity " + exception.getSystemId());
                    // TODO throw an exception here?
                }

                public void warning(SAXParseException exception) {
                    log.error("SAX parse warning whilst reading saved manageable attributes: " +
                            exception.getMessage() +
                            " at line " + exception.getLineNumber() +
                            ", column " + exception.getColumnNumber() +
                            " in entity " + exception.getSystemId());
                }
            });
            Document doc = builder.parse(in);
            Element root = doc.getDocumentElement();
            serverOverride = new ServerOverride(root);
        } catch (SAXException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (ParserConfigurationException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (InvalidGBeanException e) {
            log.error("Unable to read saved manageable attributes", e);
        } finally {
            if (fis != null)
                fis.close();
        }
    }

    public synchronized void save() throws IOException {
        if (readOnly) {
            return;
        }
        ensureParentDirectory();
        if (!tempFile.exists() && !tempFile.createNewFile()) {
            throw new IOException("Unable to create manageable attribute working file for save " + tempFile.getAbsolutePath());
        }
        if (!tempFile.canWrite()) {
            throw new IOException("Unable to write to manageable attribute working file for save " + tempFile.getAbsolutePath());
        }

        // write the new configuration to the temp file
        saveXmlToFile(tempFile, serverOverride);

        // delete the current backup file
        if (backupFile.exists()) {
            if (!backupFile.delete()) {
                throw new IOException("Unable to delete old backup file in order to back up current manageable attribute working file for save");
            }
        }

        // rename the existing configuration file to the backup file
        if (attributeFile.exists()) {
            if (!attributeFile.renameTo(backupFile)) {
                throw new IOException("Unable to rename " + attributeFile.getAbsolutePath() + " to " + backupFile.getAbsolutePath() + " in order to back up manageable attribute save file");
            }
        }

        // rename the temp file the the configuration file
        if (!tempFile.renameTo(attributeFile)) {
            throw new IOException("EXTREMELY CRITICAL!  Unable to move manageable attributes working file to proper file name!  Configuration will revert to defaults unless this is manually corrected!  (could not rename " + tempFile.getAbsolutePath() + " to " + attributeFile.getAbsolutePath() + ")");
        }
    }

    private static void saveXmlToFile(File file, ServerOverride serverOverride) {
        DocumentBuilderFactory dFactory = XmlUtil.newDocumentBuilderFactory();
        dFactory.setValidating(true);
        dFactory.setNamespaceAware(true);
        dFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                             "http://www.w3.org/2001/XMLSchema");
        dFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                             LocalAttributeManager.class.getResourceAsStream("/META-INF/schema/local-attributes-1.1.xsd"));
        FileOutputStream fos = null;
        try {
            Document doc = dFactory.newDocumentBuilder().newDocument();
            serverOverride.writeXml(doc);
            TransformerFactory xfactory = XmlUtil.newTransformerFactory();
            Transformer xform = xfactory.newTransformer();
            xform.setOutputProperty(OutputKeys.INDENT, "yes");
            xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            fos = new FileOutputStream(file);
            // use a FileOutputStream instead of a File on the StreamResult 
            // constructor as problems were encountered with the file not being closed.
            StreamResult sr = new StreamResult(fos); 
            xform.transform(new DOMSource(doc), sr);
        } catch (FileNotFoundException e) {
            // file is directory or cannot be created/opened
            log.error("Unable to write config.xml", e);
        } catch (ParserConfigurationException e) {
            log.error("Unable to write config.xml", e);
        } catch (TransformerException e) {
            log.error("Unable to write config.xml", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    //PersistentConfigurationList
    public synchronized boolean isKernelFullyStarted() {
        return kernelFullyStarted;
    }

    public synchronized void setKernelFullyStarted(boolean kernelFullyStarted) {
        this.kernelFullyStarted = kernelFullyStarted;
    }

    public synchronized List restore() throws IOException {
        List configs = new ArrayList();
        for (Iterator iterator = serverOverride.getConfigurations().entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            ConfigurationOverride configuration = (ConfigurationOverride) entry.getValue();
            if (configuration.isLoad()) {
                Artifact configID = (Artifact) entry.getKey();
                configs.add(configID);
            }
        }
        return configs;
    }

    public void startConfiguration(Artifact configurationName) {
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, false);
        if(configuration == null) {
            return;
        }
        configuration.setLoad(true);
        attributeChanged();
    }

    public synchronized void addConfiguration(Artifact configurationName) {
        // Check whether we have it already
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, false);
        // If not, initialize it
        if(configuration == null) {
            configuration = serverOverride.getConfiguration(configurationName, true);
            configuration.setLoad(false);
            attributeChanged();
        }
    }

    public synchronized void removeConfiguration(Artifact configName) {
        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return;
        }
        serverOverride.removeConfiguration(configName);
        attributeChanged();
    }

    public Artifact[] getListedConfigurations(Artifact query) {
        return serverOverride.queryConfigurations(query);
    }

    public void stopConfiguration(Artifact configName) {
        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return;
        }
        configuration.setLoad(false);
        attributeChanged();
    }

    public void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration) {
        ConfigurationOverride configInfo = serverOverride.getConfiguration(oldName);
        if(configInfo == null) {
            throw new IllegalArgumentException("Trying to migrate unknown configuration: " + oldName);
        }
        serverOverride.removeConfiguration(oldName);
        configInfo = new ConfigurationOverride(configInfo, newName);
        //todo: check whether all the attributes are still valid for the new configuration
        serverOverride.addConfiguration(configInfo);
        attributeChanged();
    }

    //GBeanLifeCycle
    public synchronized void doStart() throws Exception {
        load();
        if (!readOnly) {
            timer = new Timer();
        }
        log.debug("Started LocalAttributeManager with data on " + serverOverride.getConfigurations().size() + " configurations");
    }

    public synchronized void doStop() throws Exception {
        boolean doSave = false;
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                    doSave = true;
                }
            }
        }
        if (doSave) {
            save();
        }
        log.debug("Stopped LocalAttributeManager with data on " + serverOverride.getConfigurations().size() + " configurations");
        serverOverride = new ServerOverride();
    }

    public synchronized void doFail() {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
        }
        serverOverride = new ServerOverride();
    }

    private synchronized void ensureParentDirectory() throws IOException {
        if (attributeFile == null) {
            attributeFile = serverInfo.resolveServer(configFile);
            tempFile = new File(attributeFile.getAbsolutePath() + TEMP_EXTENSION);
            backupFile = new File(attributeFile.getAbsolutePath() + BACKUP_EXTENSION);
        }
        File parent = attributeFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory for list:" + parent);
            }
        }
        if (!parent.canRead() || !parent.canWrite()) {
            throw new IOException("Unable to write manageable attribute files to directory " + parent.getAbsolutePath());
        }
    }


    private synchronized void attributeChanged() {
        if (currentTask != null) {
            currentTask.cancel();
        }
        if (timer != null) {
            currentTask = new TimerTask() {

                public void run() {
                    try {
                        LocalAttributeManager.this.save();
                    } catch (IOException e) {
                        log.error("Error saving attributes", e);
                    }
                }
            };
            timer.schedule(currentTask, SAVE_BUFFER_MS);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LocalAttributeManager.class, "AttributeStore");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("configFile", String.class, true);
        infoFactory.addAttribute("readOnly", boolean.class, true);
        infoFactory.addInterface(ManageableAttributeStore.class);
        infoFactory.addInterface(PersistentConfigurationList.class);

        infoFactory.setConstructor(new String[]{"configFile", "readOnly", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
