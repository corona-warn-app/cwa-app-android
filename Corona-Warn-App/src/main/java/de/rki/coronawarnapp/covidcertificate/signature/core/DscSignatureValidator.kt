package de.rki.coronawarnapp.covidcertificate.signature.core

import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder.DscMessage.Algorithm.ES256
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder.DscMessage.Algorithm.PS256
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.QrCodeString
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.DERSequence
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

@Reusable
class DscSignatureValidator @Inject constructor(
    private val dccCoseDecoder: DccCoseDecoder,
    private val dccQrCodeExtractor: DccQrCodeExtractor
) {

    /**
     * @throws InvalidHealthCertificateException
     */
    suspend fun isSignatureValid(dscData: DscData, qrCodeString: QrCodeString): Boolean {
        Timber.tag(TAG).d("isSignatureValid(dscData=%s,certificateData=%s)", dscData, qrCodeString)
        val coseObject = dccQrCodeExtractor.extractCoseObject(qrCodeString)
        val dscMessage = dccCoseDecoder.decodeDscMessage(coseObject)
        val signedPayload = CBORObject.NewArray().apply {
            Add("Signature1")
            Add(dscMessage.protectedHeader)
            Add(ByteArray(0))
            Add(dscMessage.payload)
        }.EncodeToBytes()

        val signedPayloadHash = signedPayload.toSHA256().toByteArray()
        val signature = dscMessage.signature
        val verifier = when (dscMessage.alg) {
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

        // TODO first/second of pair:/
        val filteredDscSet = dscData.dscList.filter { it.first.toString() == dscMessage.kid }
        val matchedDscSet = when {
            filteredDscSet.isEmpty() || dscMessage.kid.isEmpty() -> dscData.dscList
            else -> filteredDscSet
        }

        return true
    }

    companion object {
        private const val TAG = "DscSignatureValidator"
    }

    private fun ByteArray.splitHalves(): Pair<ByteArray, ByteArray> =
        take(size / 2).toByteArray() to drop(size / 2).toByteArray()
}
