/*
  Copyright (C) 2021 T-Systems International GmbH and all other contributors

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Modifications Copyright (c) 2021 SAP SE or an SAP affiliate company.
 */
package de.rki.coronawarnapp.covidcertificate.signature.core

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import org.bouncycastle.asn1.pkcs.RSAPublicKey
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec

fun DscMessage.signedPayload(): ByteArray = CBORObject.NewArray().apply {
    Add("Signature1")
    Add(protectedHeader.toByteArray())
    Add(ByteArray(0))
    Add(payload.toByteArray())
}.EncodeToBytes()

fun PublicKey.toRsaPublicKey(): PublicKey {
    val bytes = SubjectPublicKeyInfo.getInstance(this.encoded).publicKeyData.bytes
    val rsaPublicKey = RSAPublicKey.getInstance(bytes)
    val spec = RSAPublicKeySpec(rsaPublicKey.modulus, rsaPublicKey.publicExponent)
    return KeyFactory.getInstance("RSA").generatePublic(spec)
}
