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

    ==============================

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
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage.Algorithm.ES256
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage.Algorithm.PS256
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_EXPIRED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NOT_YET_VALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NO_MATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_RC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_TC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_VC
import kotlinx.coroutines.flow.first
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.pkcs.RSAPublicKey
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateFactory
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate
import java.security.spec.RSAPublicKeySpec
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor(
    private val dscRepository: DscRepository
) {

    init {
        Security.addProvider(BouncyCastleProvider()) // For SHA256withRSA/PSS
    }

    private val vcOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.2",
        "1.3.6.1.4.1.0.1847.2021.1.2"
    )
    private val tcOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.1",
        "1.3.6.1.4.1.0.1847.2021.1.1"
    )
    private val rcOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.3",
        "1.3.6.1.4.1.0.1847.2021.1.3"
    )

    private val oidSet = vcOids + tcOids + rcOids

    /**
     * @throws InvalidHealthCertificateException if validation fail, otherwise it is OK!
     */
    suspend fun validateSignature(dccData: DccData<*>) {
        val dscData = dscRepository.dscData.first()
        Timber.tag(TAG).d("validateSignature(dscListSize=%s)", dscData.dscList.size)
        val dscMessage = dccData.dscMessage
        val signedPayload = CBORObject.NewArray().apply {
            Add("Signature1")
            Add(dscMessage.protectedHeader.toByteArray())
            Add(ByteArray(0))
            Add(dscMessage.payload.toByteArray())
        }.EncodeToBytes()

        findDscCertificate(dscData, dscMessage, signedPayload).apply {
            validate()
            checkCertOid(dccData)
        }
    }

    private fun findDscCertificate(
        dscData: DscData,
        dscMessage: DscMessage,
        toVerify: ByteArray
    ): X509Certificate {
        val filteredDscSet = dscData.dscList.filter { it.kid == dscMessage.kid }
        Timber.d("filteredDscSetSize=${filteredDscSet.size}")

        val matchedDscSet = when {
            filteredDscSet.isEmpty() || dscMessage.kid.isEmpty() -> dscData.dscList
            else -> filteredDscSet
        }
        Timber.d("matchedDscSetSize=${matchedDscSet.size}")

        var x509Certificate: X509Certificate? = null
        for (dsc in matchedDscSet) {
            val dscCertificate = x509certificate(dsc)
            val (publicKey, signature) = when (dscMessage.algorithm) {
                ES256 -> dscCertificate.publicKey to dscMessage.signature.toByteArray().toECDSAVerifier()
                PS256 -> dscCertificate.publicKey.toRsaPublicKey() to dscMessage.signature.toByteArray()
            }

            try {
                val valid = Signature.getInstance(dscMessage.algorithm.algName).verify(publicKey, toVerify, signature)
                Timber.d("Dsc certificate (${dsc.kid}) is valid=$valid")

                if (valid) {
                    x509Certificate = dscCertificate
                    break
                }
            } catch (ignored: Exception) {
                // Ignore errors / continue
            }
        }

        return x509Certificate ?: throw InvalidHealthCertificateException(HC_DSC_NO_MATCH)
    }

    private fun x509certificate(dscItem: DscItem): X509Certificate {
        return ByteArrayInputStream(dscItem.data.toByteArray()).use {
            CertificateFactory.getInstance("X.509").generateCertificate(it)
        } as X509Certificate
    }

    private fun ByteArray.toECDSAVerifier(): ByteArray {
        val (r, s) = splitHalves()
        return DERSequence(
            arrayOf(
                ASN1Integer(BigInteger(1, r)),
                ASN1Integer(BigInteger(1, s)),
            )
        ).encoded
    }

    private fun X509Certificate.validate() {
        try {
            checkValidity()
        } catch (e: CertificateExpiredException) {
            throw InvalidHealthCertificateException(HC_DSC_EXPIRED)
        } catch (e: CertificateNotYetValidException) {
            throw InvalidHealthCertificateException(HC_DSC_NOT_YET_VALID)
        }
    }

    private fun X509Certificate.checkCertOid(dccData: DccData<*>) {
        val extendedKeysIntersect = extendedKeyUsage.orEmpty().toSet() intersect oidSet
        if (extendedKeysIntersect.isEmpty()) return // OK!
        when (dccData.certificate) {
            is VaccinationDccV1 -> if (vcOids.intersect(extendedKeysIntersect).isEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_VC)

            is TestDccV1 -> if (tcOids.intersect(extendedKeysIntersect).isEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_TC)

            is RecoveryDccV1 -> if (rcOids.intersect(extendedKeysIntersect).isEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_RC)
        }
    }

    private fun PublicKey.toRsaPublicKey(): PublicKey {
        val bytes = SubjectPublicKeyInfo.getInstance(this.encoded).publicKeyData.bytes
        val rsaPublicKey = RSAPublicKey.getInstance(bytes)
        val spec = RSAPublicKeySpec(rsaPublicKey.modulus, rsaPublicKey.publicExponent)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    private fun Signature.verify(
        verificationKey: PublicKey,
        toVerify: ByteArray,
        signature: ByteArray
    ): Boolean {
        initVerify(verificationKey)
        update(toVerify)
        return verify(signature)
    }

    private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
        take(size / 2).toByteArray() to drop(size / 2).toByteArray()

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
