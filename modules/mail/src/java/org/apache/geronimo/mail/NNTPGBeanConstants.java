/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.mail;

/**
 * Common constants used by the two NNTP GBean classes.
 * <p/>
 *
 * @version $Rev$ $Date$
 */
public interface NNTPGBeanConstants {

    // the NNTP configuration property names
    static public final String NNTP_PORT = "mail.nntp.port";
    static public final String NNTP_CONNECTION_TIMEOUT = "mail.nntp.connectiontimeout";
    static public final String NNTP_TIMEOUT = "mail.nntp.timeout";
    static public final String NNTP_FROM = "mail.nntp.from";
    static public final String NNTP_AUTH = "mail.nntp.auth";
    static public final String NNTP_REALM = "mail.nntp.sasl.realm";
    static public final String NNTP_QUITWAIT = "mail.nntp.quitwait";
    static public final String NNTP_FACTORY_CLASS = "mail.nntp.socketFactory.class";
    static public final String NNTP_FACTORY_FALLBACK = "mail.nntp.socketFactory.fallback";
    static public final String NNTP_FACTORY_PORT = "mail.nntp.socketFactory.port";
}

