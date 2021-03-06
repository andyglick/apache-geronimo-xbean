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
package org.apache.geronimo.management;

/**
 * Represents the JSR-77 type with the same name
 *
 * @version $Rev$ $Date$
 */
public interface JDBCDataSource extends J2EEManagedObject {
    /**
     * The driver used by this data source to connect to the database.
     * @see "JSR77.3.26.1.1"
     * @return the ObjectName of the JDBCDriver.
     */
    String getJDBCDataSources();
}
