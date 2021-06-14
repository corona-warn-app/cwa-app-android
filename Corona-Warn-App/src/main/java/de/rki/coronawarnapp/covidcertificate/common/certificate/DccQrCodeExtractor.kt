package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.bugreporting.censors.vaccination.DccQrCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccHeaderParser
import de.rki.coronawarnapp.covidcertificate.common.decoder.RawCOSEObject
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.util.compression.deflate
import de.rki.coronawarnapp.util.compression.inflate
import de.rki.coronawarnapp.util.encoding.Base45Decoder
import timber.log.Timber
import javax.inject.Inject

class DccQrCodeExtractor @Inject constructor(
    private val coseDecoder: DccCoseDecoder,
    private val headerParser: DccHeaderParser,
    private val bodyParser: DccV1Parser,
) : QrCodeExtractor<DccQrCode> {

    override fun canHandle(rawString: String): Boolean = rawString.startsWith(PREFIX)

    /**
     * May throw an **[InvalidTestCertificateException]**
     */
    fun extract(
        decryptionKey: ByteArray,
        rawCoseObjectEncrypted: ByteArray,
    ): DccQrCode {
        val rawCoseObject = rawCoseObjectEncrypted.decrypt(decryptionKey)
        return TestCertificateQRCode(
            data = rawCoseObject.parse(DccV1Parser.Mode.CERT_SINGLE_STRICT),
            qrCode = rawCoseObject.encode()
        )
    }

    private fun RawCOSEObject.decrypt(decryptionKey: ByteArray): RawCOSEObject = try {
        coseDecoder.decryptMessage(
            input = this,
            decryptionKey = decryptionKey
        )
    } catch (e: InvalidHealthCertificateException) {
        throw InvalidTestCertificateException(e.errorCode)
    } catch (e: Throwable) {
        Timber.e(e, InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID.toString())
        throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID)
    }

    private fun RawCOSEObject.encode(): String {
        return PREFIX + compress().encodeBase45()
    }

    private fun ByteArray.encodeBase45(): String = try {
        Base45Decoder.encode(this)
    } catch (e: Throwable) {
        Timber.e(e, InvalidHealthCertificateException.ErrorCode.HC_BASE45_ENCODING_FAILED.toString())
        throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.HC_BASE45_ENCODING_FAILED)
    }

    private fun RawCOSEObject.compress(): ByteArray = try {
        this.deflate()
    } catch (e: Throwable) {
        Timber.e(e, InvalidHealthCertificateException.ErrorCode.HC_ZLIB_COMPRESSION_FAILED.toString())
        throw InvalidTestCertificateException(InvalidHealthCertificateException.ErrorCode.HC_ZLIB_COMPRESSION_FAILED)
    }

    override fun extract(rawString: String): DccQrCode {
        DccQrCodeCensor.addQRCodeStringToCensor(rawString)

        val parsedData = rawString
            .removePrefix(PREFIX)
            .decodeBase45()
            .decompress()
            .parse(DccV1Parser.Mode.CERT_SINGLE_STRICT) // TODO use mode

        return VaccinationCertificateQRCode(
            qrCode = rawString,
            data = parsedData,
        )
    }

    fun extract(rawString: String, mode: DccV1Parser.Mode): DccQrCode {
        DccQrCodeCensor.addQRCodeStringToCensor(rawString)

        val parsedData = rawString
            .removePrefix(PREFIX)
            .decodeBase45()
            .decompress()
            .parse(mode)

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

    fun RawCOSEObject.parse(mode: DccV1Parser.Mode): DccData = try {
        Timber.v("Parsing COSE for covid certificate.")
        val cbor = coseDecoder.decode(this)

        DccData(
            header = headerParser.parse(cbor),
            certificate = bodyParser.parse(cbor, mode)
        ).also {
            DccQrCodeCensor.addCertificateToCensor(it)
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
