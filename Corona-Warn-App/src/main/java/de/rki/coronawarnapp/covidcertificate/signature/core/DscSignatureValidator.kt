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
import de.rki.coronawarnapp.server.protocols.internal.dgc.DscListOuterClass
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.encoding.base64
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
class DscSignatureValidator @Inject constructor() {

    init {
        Security.addProvider(BouncyCastleProvider()) // for SHA256withRSA/PSS
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
    fun validateSignature(dscData: DscData, dccData: DccData<*>) {
        Timber.tag(TAG).d("validateSignature(dscData=%s,dccData=%s)", dscData, dccData)
        val dscMessage = dccData.dscMessage

        val signedPayload = CBORObject.NewArray().apply {
            Add("Signature1")
            Add(dscMessage.protectedHeader)
            Add(ByteArray(0))
            Add(dscMessage.payload)
        }.EncodeToBytes()
        val signedPayloadHash = signedPayload.toSHA256().toByteArray()
        findDscCertificate(dscData, dscMessage, signedPayloadHash).apply {
            validate()
            checkCertOid(dccData)
        }
    }

    private fun findDscCertificate(
        dscData: DscData,
        dscMessage: DscMessage,
        dataToVerify: ByteArray
    ): X509Certificate {
        val filteredDscSet = dscData.dscList.filter { it.kid.toByteArray().base64() == dscMessage.kid }
        val matchedDscSet = when {
            filteredDscSet.isEmpty() || dscMessage.kid.isEmpty() -> dscData.dscList
            else -> filteredDscSet
        }

        var x509Certificate: X509Certificate? = null
        for (dsc in matchedDscSet) {
            val dscCertificate = x509certificate(dsc)
            val (publicKey, signature) = when (dscMessage.algorithm) {
                ES256 -> dscCertificate.publicKey to dscMessage.signature.toByteArray().toECDSAVerifier()
                PS256 -> dscCertificate.publicKey.toRsaPublicKey() to dscMessage.signature.toByteArray()
            }

            try {
                val valid = Signature.getInstance(dscMessage.algorithm.algName).verify(
                    publicKey,
                    dataToVerify,
                    signature
                )

                Timber.d("valid=$valid")

                if (valid) {
                    x509Certificate = dscCertificate
                    break
                }
            } catch (ignored: Exception) {
                // Ignore errors
                ignored.printStackTrace() // TODO remove 
            }
        }

        return x509Certificate ?: throw InvalidHealthCertificateException(HC_DSC_NO_MATCH)
    }

    private fun x509certificate(dscListItem: DscListOuterClass.DscListItem): X509Certificate {
        return ByteArrayInputStream(dscListItem.data.toByteArray()).use {
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
        dataToBeVerify: ByteArray,
        signature: ByteArray
    ): Boolean {
        initVerify(verificationKey)
        update(dataToBeVerify)
        return verify(signature)
    }

    private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
        take(size / 2).toByteArray() to drop(size / 2).toByteArray()

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
