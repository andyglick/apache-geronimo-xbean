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
package org.apache.geronimo.deployment.scanner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * An MBean that maintains a list of URLs and periodically invokes a Scanner
 * to search them for deployments. The set found is sent out as a JMX
 * Notifiction.
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/11 10:41:19 $
 */
public class DeploymentScanner extends NotificationBroadcasterSupport implements DeploymentScannerMBean,MBeanRegistration {
    private static final Logger log = Logger.getLogger(DeploymentScanner.class);
    private final Map scanners = new HashMap();
    private long scanInterval;
    private boolean run;
    private Thread scanThread;
    private int sequence = 0;
    private ObjectName objectName;

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        this.objectName = objectName;
        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public synchronized long getScanInterval() {
        return scanInterval;
    }

    public synchronized void setScanInterval(long scanInterval) {
        this.scanInterval = scanInterval;
    }

    public void addURL(String url, boolean recurse) throws MalformedURLException {
        addURL(new URL(url), recurse);
    }

    public synchronized void addURL(URL url, boolean recurse) {
        if (!scanners.containsKey(url)) {
            Scanner scanner = getScannerForURL(url, recurse);
            scanners.put(url, scanner);
        }
    }

    private Scanner getScannerForURL(URL url, boolean recurse) {
        String protocol = url.getProtocol();
        if ("file".equals(protocol)) {
            return new FileSystemScanner(new File(url.getFile()), recurse);
        } else if ("http".equals(protocol) || "https".equals(protocol)) {
            return new WebDAVScanner(url, recurse);
        } else {
            throw new IllegalArgumentException("Unknown protocol "+protocol);
        }
    }

    public void removeURL(String url) throws MalformedURLException {
        removeURL(new URL(url));
    }

    public synchronized void removeURL(URL url) {
        scanners.remove(url);
    }

    public synchronized void start() {
        if (scanThread == null) {
            run = true;
            scanThread = new Thread() {
                public void run() {
                    synchronized (DeploymentScanner.this) {
                        while (run) {
                            scanNow();
                            try {
                                DeploymentScanner.this.wait(scanInterval);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }
            };
            scanThread.start();
        }
    }

    public synchronized void stop() {
        run = false;
        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }
    }

    public synchronized void scanNow() {
        Set results = new HashSet();
        for (Iterator i = scanners.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            URL url = (URL) entry.getKey();
            log.debug("Starting scan of "+url);
            Scanner scanner = (Scanner) entry.getValue();
            try {
                Set result = scanner.scan();
                log.debug("Finished scan of "+url+", "+result.size()+" deployment(s) found");
                results.addAll(result);
            } catch (IOException e) {
                log.error("Error scanning url "+url,  e);
            }
        }

        Notification notification = new Notification(SCAN_COMPLETE, objectName, ++sequence);
        notification.setUserData(Collections.unmodifiableSet(results));
        sendNotification(notification);
    }
}
