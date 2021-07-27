/*
    Copyright (C) 2021 IBM Deutschland GmbH

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

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import java.math.BigInteger

fun ByteArray.toECDSAVerifier(): ByteArray {
    val (r, s) = splitHalves()
    return DERSequence(
        arrayOf(
            ASN1Integer(BigInteger(1, r)),
            ASN1Integer(BigInteger(1, s)),
        )
    ).encoded
}

private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
    take(size / 2).toByteArray() to drop(size / 2).toByteArray()
