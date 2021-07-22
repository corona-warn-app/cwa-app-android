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
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NO_MATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_RC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_TC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_VC
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import okio.ByteString
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.math.BigInteger
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor(
    private val timeStamper: TimeStamper
) {

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
    suspend fun validateSignature(dscData: DscData, dccData: DccData<*>) {
        Timber.tag(TAG).d("isSignatureValid(dscData=%s,dccData=%s)", dscData, dccData)
        val dscMessage = dccData.dscMessage

        val signedPayload = CBORObject.NewArray().apply {
            Add("Signature1")
            Add(dscMessage.protectedHeader)
            Add(ByteArray(0))
            Add(dscMessage.payload)
        }.EncodeToBytes()
        val signedPayloadHash = signedPayload.toSHA256().toByteArray()
        val signature = dscMessage.signature
        val verifier = when (dscMessage.algorithm) {
            PS256 -> signature
            ES256 -> {
                val (r, s) = signature.splitHalves()
                DERSequence(
                    arrayOf(
                        ASN1Integer(BigInteger(1, r)),
                        ASN1Integer(BigInteger(1, s)),
                    )
                ).encoded
            }
        }

        val dsc = findDscForDgc(dscData, dscMessage, verifier, signedPayloadHash)
        // TODO validate Dsc against Validation Clock
        x509certificate(dsc).checkCertOid(dccData)
    }

    private fun findDscForDgc(
        dscData: DscData,
        dscMessage: DscMessage,
        verifier: ByteArray,
        signedPayloadHash: ByteArray
    ): Pair<ByteString, ByteString> {
        val filteredDscSet = dscData.dscList.filter { it.first.toString() == dscMessage.kid }
        val matchedDscSet = when {
            filteredDscSet.isEmpty() || dscMessage.kid.isEmpty() -> dscData.dscList
            else -> filteredDscSet
        }

        val dsc = matchedDscSet.firstOrNull { dsc ->
            val x509Certificate = x509certificate(dsc)

            Signature.getInstance(dscMessage.algorithm.algName).verify(
                x509Certificate.publicKey, // TODO  Check Public key for different algorithms
                verifier,
                signedPayloadHash
            )
        }

        return dsc ?: throw InvalidHealthCertificateException(HC_DSC_NO_MATCH)
    }

    private fun x509certificate(dsc: Pair<ByteString, ByteString>): X509Certificate {
        return ByteArrayInputStream(dsc.second.toByteArray()).use {
            CertificateFactory.getInstance("X.509").generateCertificate(it)
        } as X509Certificate
    }

    private fun X509Certificate.checkCertOid(dccData: DccData<*>) {
        val extendedKeys = extendedKeyUsage.orEmpty().toSet() intersect oidSet
        if (extendedKeys.isEmpty()) return // OK!
        when (dccData.certificate) {
            is VaccinationDccV1 -> if (vcOids.intersect(extendedKeys).isNotEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_VC)

            is TestDccV1 -> if (tcOids.intersect(extendedKeys).isNotEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_TC)

            is RecoveryDccV1 -> if (rcOids.intersect(extendedKeys).isNotEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_RC)
        }
    }

    private fun Signature.verify(
        publicKey: PublicKey,
        verifier: ByteArray,
        toVerify: ByteArray
    ): Boolean {
        initVerify(publicKey)
        update(verifier)

        return verify(toVerify)
    }

    private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
        take(size / 2).toByteArray() to drop(size / 2).toByteArray()

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
