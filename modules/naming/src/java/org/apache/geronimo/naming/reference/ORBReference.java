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
package org.apache.geronimo.naming.reference;

import javax.management.ObjectName;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public class ORBReference extends SimpleAwareReference {
    private final ObjectName corbaGBean;

    public ORBReference(ObjectName corbaGBean) {
        this.corbaGBean = corbaGBean;
    }

    public String getClassName() {
        return "org.omg.CORBA.ORB";
    }

    public Object getContent() throws NamingException {
        Kernel kernel = getKernel();
        try {
            return kernel.getAttribute(corbaGBean, "ORB");
        } catch (Exception e) {
            throw (NameNotFoundException) new NameNotFoundException("Error getting ORB attribut from CORBAGBean: objectName=" + corbaGBean).initCause(e);
        }
    }
}
