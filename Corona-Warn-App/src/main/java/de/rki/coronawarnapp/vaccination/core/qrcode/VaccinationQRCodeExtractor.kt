package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.decoder.Base45Decoder
import de.rki.coronawarnapp.vaccination.decoder.COSEDecoder
import de.rki.coronawarnapp.vaccination.decoder.ZLIBDecompressor
import timber.log.Timber
import javax.inject.Inject

class VaccinationQRCodeExtractor @Inject constructor(
    private val base45Decoder: Base45Decoder,
    private val ZLIBDecompressor: ZLIBDecompressor,
    private val COSEDecoder: COSEDecoder,
    private val VaccinationCertificateV1Decoder: VaccinationCertificateV1Decoder,
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
            .extractCBORObject()
            .decodeCBORObject()
        return VaccinationCertificateQRCode(
            parsedData = VaccinationCertificateData(certificate),
            certificateCOSE = rawCOSEObject,
        )
    }

    private fun String.decodeBase45(): ByteArray {
        return try {
            base45Decoder.decode(this)
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidVaccinationQRCodeException(HC_BASE45_DECODING_FAILED)
        }
    }

    private fun ByteArray.decompress(): ByteArray {
        return try {
            ZLIBDecompressor.decode(this)
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidVaccinationQRCodeException(HC_ZLIB_DECOMPRESSION_FAILED)
        }
    }

    private fun ByteArray.extractCBORObject(): CBORObject {
        return try {
            COSEDecoder.decode(this)
        } catch (e: InvalidVaccinationQRCodeException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidVaccinationQRCodeException(HC_COSE_MESSAGE_INVALID)
        }
    }

    private fun CBORObject.decodeCBORObject(): VaccinationCertificateV1 {
        return try {
            VaccinationCertificateV1Decoder.decode(this)
        } catch (e: InvalidVaccinationQRCodeException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidVaccinationQRCodeException(HC_CBOR_DECODING_FAILED)
        }
    }
}
