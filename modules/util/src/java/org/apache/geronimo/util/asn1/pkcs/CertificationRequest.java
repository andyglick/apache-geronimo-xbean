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

package org.apache.geronimo.util.asn1.pkcs;

import org.apache.geronimo.util.asn1.ASN1Encodable;
import org.apache.geronimo.util.asn1.ASN1EncodableVector;
import org.apache.geronimo.util.asn1.ASN1Sequence;
import org.apache.geronimo.util.asn1.DERBitString;
import org.apache.geronimo.util.asn1.DERObject;
import org.apache.geronimo.util.asn1.DERSequence;
import org.apache.geronimo.util.asn1.x509.AlgorithmIdentifier;

/**
 * PKCS10 Certification request object.
 * <pre>
 * CertificationRequest ::= SEQUENCE {
 *   certificationRequestInfo  CertificationRequestInfo,
 *   signatureAlgorithm        AlgorithmIdentifier{{ SignatureAlgorithms }},
 *   signature                 BIT STRING
 * }
 * </pre>
 */
public class CertificationRequest
    extends ASN1Encodable
{
    protected CertificationRequestInfo reqInfo = null;
    protected AlgorithmIdentifier sigAlgId = null;
    protected DERBitString sigBits = null;

    protected CertificationRequest()
    {
    }

    public CertificationRequest(
        CertificationRequestInfo requestInfo,
        AlgorithmIdentifier     algorithm,
        DERBitString            signature)
    {
        this.reqInfo = requestInfo;
        this.sigAlgId = algorithm;
        this.sigBits = signature;
    }

    public CertificationRequest(
        ASN1Sequence seq)
    {
        reqInfo = CertificationRequestInfo.getInstance(seq.getObjectAt(0));
        sigAlgId = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        sigBits = (DERBitString)seq.getObjectAt(2);
    }

    public CertificationRequestInfo getCertificationRequestInfo()
    {
        return reqInfo;
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return sigAlgId;
    }

    public DERBitString getSignature()
    {
        return sigBits;
    }

    public DERObject toASN1Object()
    {
        // Construct the CertificateRequest
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(reqInfo);
        v.add(sigAlgId);
        v.add(sigBits);

        return new DERSequence(v);
    }
}
