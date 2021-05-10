package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.vaccination.core.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.decoder.Base45Decoder
import de.rki.coronawarnapp.vaccination.decoder.ZLIBDecompressor
import timber.log.Timber
import javax.inject.Inject

class VaccinationQRCodeExtractor @Inject constructor(
    private val base45Decoder: Base45Decoder,
    private val zLIBDecompressor: ZLIBDecompressor,
    private val healthCertificateCOSEDecoder: HealthCertificateCOSEDecoder,
    private val vaccinationCertificateV1Parser: VaccinationCertificateV1Parser,
) : QrCodeExtractor<VaccinationCertificateQRCode> {

    private val prefix = "HC1:"

    override fun canHandle(rawString: String): Boolean {
        return rawString.startsWith(prefix)
    }

    override fun extract(rawString: String): VaccinationCertificateQRCode {
        val rawCOSEObject = rawString
            .removePrefix(prefix)
            .decodeBase45()
            .decompress()
        val certificate = rawCOSEObject
            .decodeCOSEObject()
            .decodeCBORObject()
        return VaccinationCertificateQRCode(
            parsedData = certificate,
            certificateCOSE = rawCOSEObject,
        )
    }

    private fun String.decodeBase45(): ByteArray = try {
        base45Decoder.decode(this)
    } catch (e: Exception) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun ByteArray.decompress(): RawCOSEObject = try {
        RawCOSEObject(zLIBDecompressor.decode(this))
    } catch (e: Exception) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    private fun RawCOSEObject.decodeCOSEObject(): CBORObject = try {
        healthCertificateCOSEDecoder.decode(this)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun CBORObject.decodeCBORObject(): VaccinationCertificateData = try {
        vaccinationCertificateV1Parser.decode(this)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Exception) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }
}
