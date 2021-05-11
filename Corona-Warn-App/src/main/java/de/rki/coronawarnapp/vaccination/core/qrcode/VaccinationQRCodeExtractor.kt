package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.util.encoding.decodeBase45
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.certificate.VaccinationDGCV1Parser
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.decoder.ZLIBDecompressor
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

class VaccinationQRCodeExtractor @Inject constructor(
    private val zLIBDecompressor: ZLIBDecompressor,
    private val healthCertificateCOSEDecoder: HealthCertificateCOSEDecoder,
    private val headerParser: HealthCertificateHeaderParser,
    private val vaccinationDGCV1Parser: VaccinationDGCV1Parser,
) : QrCodeExtractor<VaccinationCertificateQRCode> {

    override fun canHandle(rawString: String): Boolean = rawString.startsWith(PREFIX)

    override fun extract(rawString: String): VaccinationCertificateQRCode {
        val rawCOSEObject = rawString
            .removePrefix(PREFIX)
            .tryDecodeBase45()
            .decompress()

        val cbor = rawCOSEObject.decodeCOSEObject()
        val certificate = vaccinationDGCV1Parser.parse(cbor)

        val header = headerParser.decode(cbor)

        return VaccinationCertificateQRCode(
            parsedData = VaccinationCertificateData(
                header = header,
                certificate = certificate,
            ),
            certificateCOSE = rawCOSEObject,
        )
    }

    private fun String.tryDecodeBase45(): ByteString = try {
        this.decodeBase45()
    } catch (e: Exception) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun ByteString.decompress(): RawCOSEObject = try {
        RawCOSEObject(zLIBDecompressor.decode(this.toByteArray()))
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

    companion object {
        private val PREFIX = "HC1:"
    }
}
