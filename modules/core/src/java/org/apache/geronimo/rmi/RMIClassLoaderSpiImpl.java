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

package org.apache.geronimo.rmi;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;

import java.util.StringTokenizer;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;

/**
 * An implementation of {@link RMIClassLoaderSpi} which provides normilzation
 * of codebase URLs and delegates to the default {@link RMIClassLoaderSpi}.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/29 12:30:37 $
 */
public class RMIClassLoaderSpiImpl
    extends RMIClassLoaderSpi
{
    private RMIClassLoaderSpi delegate = RMIClassLoader.getDefaultProviderInstance();
    
    public Class loadClass(String codebase, String name, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        if (codebase != null) {
            codebase = normalizeCodebase(codebase);
        }
        
        return delegate.loadClass(codebase, name, defaultLoader);
    }
    
    public Class loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
        throws MalformedURLException, ClassNotFoundException
    {
        if (codebase != null) {
            codebase = normalizeCodebase(codebase);
        }
        
        return delegate.loadProxyClass(codebase, interfaces, defaultLoader);
    }
    
    public ClassLoader getClassLoader(String codebase)
        throws MalformedURLException
    {
        if (codebase != null) {
            codebase = normalizeCodebase(codebase);
        }
        
        return delegate.getClassLoader(codebase);
    }
    
    public String getClassAnnotation(Class type) {
        return delegate.getClassAnnotation(type);
    }
    
    static String normalizeCodebase(String codebase)
        throws MalformedURLException
    {
        assert codebase != null;
        
        StringBuffer codebaseBuff = new StringBuffer();
        StringBuffer buff = new StringBuffer();
        StringTokenizer stok = new StringTokenizer(codebase, " \t\n\r\f", true);
        
        while (stok.hasMoreTokens()) {
            String item = stok.nextToken();
            try {
                URL url = new URL(item);
                
                // If we got this far then item is a valid url, so commit the current
                // buffer and start collecting any trailing bits from where we are now
                
                // Put spaces back in for URL delims
                if (codebaseBuff.length() != 0) {
                    codebaseBuff.append(" ");
                }
                
                // Normalize the URL
                url = normalizeURL(new URL(buff.toString()));
                codebaseBuff.append(url);
                
                // Reset the working buffer
                buff.setLength(0);
            }
            catch (MalformedURLException ignore) {
                // just keep going & append to the working buffer
            }
            
            buff.append(item);
        }
        
        // handle trainling elements
        if (buff.length() != 0) {
            URL url = normalizeURL(new URL(buff.toString()));
            
            if (codebaseBuff.length() != 0) {
                codebaseBuff.append(" ");
            }
            codebaseBuff.append(url);
        }
        
        return codebaseBuff.toString();
    }
    
    static URL normalizeURL(URL url)
    {
        assert url != null;
        
        if (url.getProtocol().equals("file")) {
            String filename = url.getFile().replace('/', File.separatorChar);
            File file = new File(filename);
            try {
                url = file.toURI().toURL();
            }
            catch (MalformedURLException ignore) {}
        }
        
        return url;
    }
}
