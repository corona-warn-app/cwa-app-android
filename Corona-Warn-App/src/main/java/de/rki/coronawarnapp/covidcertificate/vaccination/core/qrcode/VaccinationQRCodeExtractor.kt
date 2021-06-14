package de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode

import de.rki.coronawarnapp.bugreporting.censors.vaccination.CertificateQrCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccHeaderParser
import de.rki.coronawarnapp.covidcertificate.common.decoder.RawCOSEObject
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate.VaccinationDccV1Parser
import de.rki.coronawarnapp.util.compression.inflate
import de.rki.coronawarnapp.util.encoding.Base45Decoder
import timber.log.Timber
import javax.inject.Inject

class VaccinationQRCodeExtractor @Inject constructor(
    private val coseDecoder: DccCoseDecoder,
    private val headerParser: DccHeaderParser,
    private val bodyParser: VaccinationDccV1Parser,
) : QrCodeExtractor<VaccinationCertificateQRCode> {

    override fun canHandle(rawString: String): Boolean = rawString.startsWith(PREFIX)

    override fun extract(rawString: String, mode: QrCodeExtractor.Mode): VaccinationCertificateQRCode {
        CertificateQrCodeCensor.addQRCodeStringToCensor(rawString)

        val parsedData = rawString
            .removePrefix(PREFIX)
            .decodeBase45()
            .decompress()
            .parse(lenient = mode == QrCodeExtractor.Mode.CERT_VAC_LENIENT)

        return VaccinationCertificateQRCode(
            qrCode = rawString,
            data = parsedData,
        )
    }

    private fun String.decodeBase45(): ByteArray = try {
        Base45Decoder.decode(this)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidVaccinationCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun ByteArray.decompress(): RawCOSEObject = try {
        this.inflate(sizeLimit = DEFAULT_SIZE_LIMIT)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidVaccinationCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    fun RawCOSEObject.parse(lenient: Boolean): DccData<VaccinationDccV1> = try {
        Timber.v("Parsing COSE for vaccination certificate.")
        val cbor = coseDecoder.decode(this)

        DccData(
            header = headerParser.parse(cbor),
            certificate = bodyParser.parse(cbor, lenient = lenient)
        ).also {
            CertificateQrCodeCensor.addCertificateToCensor(it)
        }.also {
            Timber.v("Parsed vaccination certificate for %s", it.certificate.nameData.familyNameStandardized)
        }
    } catch (e: InvalidHealthCertificateException) {
        throw InvalidVaccinationCertificateException(e.errorCode)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidVaccinationCertificateException(HC_CBOR_DECODING_FAILED)
    }

    companion object {
        private const val PREFIX = "HC1:"

        // Zip bomb
        private const val DEFAULT_SIZE_LIMIT = 1024L * 1024 * 10L // 10 MB
    }
}
