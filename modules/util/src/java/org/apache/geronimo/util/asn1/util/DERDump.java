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

package org.apache.geronimo.util.asn1.util;

import org.apache.geronimo.util.asn1.DEREncodable;
import org.apache.geronimo.util.asn1.DERObject;

/**
 * @deprecated use ASN1Dump.
 */
public class DERDump
    extends ASN1Dump
{
    /**
     * dump out a DER object as a formatted string
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String dumpAsString(
        DERObject   obj)
    {
        return _dumpAsString("", obj);
    }

    /**
     * dump out a DER object as a formatted string
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String dumpAsString(
        DEREncodable   obj)
    {
        return _dumpAsString("", obj.getDERObject());
    }
}
