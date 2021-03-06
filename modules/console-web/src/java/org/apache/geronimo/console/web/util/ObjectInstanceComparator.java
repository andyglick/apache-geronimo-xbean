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

package org.apache.geronimo.console.web.util;

import java.util.Comparator;
import java.util.StringTokenizer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * This class sort ObjectNames by canonical name.  Unfortunately, it
 * will not place single token domains before multiple token domains of
 * the same type (foo.bar > foo at the moment).
 *
 * @version $Rev$ $Date$
 */
public class ObjectInstanceComparator implements Comparator {
    private static final int LEFT_GREATER = 1;
    private static final int RIGHT_GREATER = -1;
    private static final int EQUAL = 0;

    public int compare(Object o1, Object o2) {
        ObjectName left = ((ObjectInstance) o1).getObjectName();
        ObjectName right = ((ObjectInstance) o2).getObjectName();
        String leftName = left.getCanonicalName();
        String rightName = right.getCanonicalName();

        StringTokenizer leftDomainTokenizer =
                new StringTokenizer(leftName, ".");

        StringTokenizer rightDomainTokenizer =
                new StringTokenizer(rightName, ".");

        while (leftDomainTokenizer.hasMoreTokens()) {
            if (!rightDomainTokenizer.hasMoreTokens()) {
                return RIGHT_GREATER;
            }
            String leftToken = leftDomainTokenizer.nextToken();
            String rightToken = rightDomainTokenizer.nextToken();
            int comparison = leftToken.compareToIgnoreCase(rightToken);
            if (comparison != 0) {
                return comparison;
            }
        }

        // left has no more tokens
        if (rightDomainTokenizer.hasMoreTokens()) {
            return LEFT_GREATER;
        }
        // both ran out of tokens so they are equal
        return EQUAL;
    }
}
