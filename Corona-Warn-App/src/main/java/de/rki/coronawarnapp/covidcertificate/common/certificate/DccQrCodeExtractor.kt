package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.bugreporting.censors.dcc.DccQrCodeCensor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_REC_LENIENT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_REC_STRICT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_SINGLE_STRICT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_TEST_LENIENT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_TEST_STRICT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_VAC_LENIENT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_VAC_STRICT
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccHeaderParser
import de.rki.coronawarnapp.covidcertificate.common.decoder.RawCOSEObject
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_BASE45_ENCODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_COMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NO_RECOVERY_ENTRY
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NO_TEST_ENTRY
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.NO_VACCINATION_ENTRY
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidRecoveryCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.recovery.core.qrcode.RecoveryCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.test.core.qrcode.TestCertificateQRCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.qrcode.VaccinationCertificateQRCode
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.util.compression.deflate
import de.rki.coronawarnapp.util.compression.inflate
import de.rki.coronawarnapp.util.encoding.Base45Decoder
import timber.log.Timber
import javax.inject.Inject

class DccQrCodeExtractor @Inject constructor(
    private val coseDecoder: DccCoseDecoder,
    private val headerParser: DccHeaderParser,
    private val bodyParser: DccV1Parser,
    private val censor: DccQrCodeCensor,
) : QrCodeExtractor<DccQrCode> {

    override suspend fun canHandle(rawString: String): Boolean = rawString.startsWith(PREFIX)

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    override suspend fun extract(rawString: String): DccQrCode =
        extract(rawString, CERT_SINGLE_STRICT, Base45Decoder.Mode.STRICT)

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    suspend fun extractEncrypted(
        decryptionKey: ByteArray,
        rawCoseObjectEncrypted: ByteArray,
    ): DccQrCode {
        val qrCodeString = rawCoseObjectEncrypted.decrypt(decryptionKey).encode()
        return extract(qrCodeString)
    }

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    suspend fun extract(
        rawString: String,
        parserMode: DccV1Parser.Mode,
        decoderMode: Base45Decoder.Mode = Base45Decoder.Mode.LENIENT
    ): DccQrCode {
        censor.addQRCodeStringToCensor(rawString)

        return try {
            val parsedData = rawString
                .removePrefix(PREFIX)
                .decodeBase45(decoderMode)
                .decompress()
                .parse(parserMode)

            toDccQrCode(rawString, parsedData).also {
                when (parserMode) {
                    CERT_VAC_STRICT, CERT_VAC_LENIENT -> if (it !is VaccinationCertificateQRCode)
                        throw InvalidVaccinationCertificateException(NO_VACCINATION_ENTRY)
                    CERT_REC_STRICT -> if (it !is RecoveryCertificateQRCode)
                        throw InvalidRecoveryCertificateException(NO_RECOVERY_ENTRY)
                    CERT_TEST_STRICT -> if (it !is TestCertificateQRCode)
                        throw InvalidTestCertificateException(NO_TEST_ENTRY)
                    else -> { /*anything goes*/
                    }
                }
            }
        } catch (e: InvalidHealthCertificateException) {
            when (parserMode) {
                CERT_VAC_STRICT, CERT_VAC_LENIENT ->
                    throw InvalidVaccinationCertificateException(e.errorCode)
                CERT_REC_STRICT, CERT_REC_LENIENT ->
                    throw InvalidRecoveryCertificateException(e.errorCode)
                CERT_TEST_STRICT, CERT_TEST_LENIENT ->
                    throw InvalidTestCertificateException(e.errorCode)
                CERT_SINGLE_STRICT -> throw e
            }
        }
    }

    private fun RawCOSEObject.decrypt(decryptionKey: ByteArray): RawCOSEObject = try {
        coseDecoder.decryptMessage(
            input = this,
            decryptionKey = decryptionKey
        )
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e, HC_COSE_MESSAGE_INVALID.toString())
        throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun RawCOSEObject.encode(): String {
        return PREFIX + compress().encodeBase45()
    }

    private fun ByteArray.encodeBase45(): String = try {
        Base45Decoder.encode(this)
    } catch (e: Throwable) {
        Timber.e(e, HC_BASE45_ENCODING_FAILED.toString())
        throw InvalidHealthCertificateException(HC_BASE45_ENCODING_FAILED)
    }

    private fun RawCOSEObject.compress(): ByteArray = try {
        this.deflate()
    } catch (e: Throwable) {
        Timber.e(e, HC_ZLIB_COMPRESSION_FAILED.toString())
        throw InvalidTestCertificateException(HC_ZLIB_COMPRESSION_FAILED)
    }

    private fun toDccQrCode(rawString: String, parsedData: DccData<DccV1.MetaData>): DccQrCode =
        when (parsedData.certificate) {
            is VaccinationDccV1 -> VaccinationCertificateQRCode(
                qrCode = rawString,
                data = DccData(
                    header = parsedData.header,
                    certificate = parsedData.certificate,
                    certificateJson = parsedData.certificateJson,
                    kid = parsedData.kid,
                    dscMessage = parsedData.dscMessage,
                ),
            )
            is TestDccV1 -> TestCertificateQRCode(
                qrCode = rawString,
                data = DccData(
                    header = parsedData.header,
                    certificate = parsedData.certificate,
                    certificateJson = parsedData.certificateJson,
                    kid = parsedData.kid,
                    dscMessage = parsedData.dscMessage,
                ),
            )
            is RecoveryDccV1 -> RecoveryCertificateQRCode(
                qrCode = rawString,
                data = DccData(
                    parsedData.header,
                    parsedData.certificate,
                    certificateJson = parsedData.certificateJson,
                    kid = parsedData.kid,
                    dscMessage = parsedData.dscMessage,
                ),
            )
            else -> throw InvalidHealthCertificateException(HC_JSON_SCHEMA_INVALID)
        }

    private fun String.decodeBase45(mode: Base45Decoder.Mode): ByteArray = try {
        Base45Decoder.decode(this, mode)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun ByteArray.decompress(): RawCOSEObject = try {
        this.inflate(sizeLimit = DEFAULT_SIZE_LIMIT)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    fun RawCOSEObject.parse(mode: DccV1Parser.Mode): DccData<DccV1.MetaData> = try {
        val message = coseDecoder.decode(this)
        val header = headerParser.parse(message.payload)
        val body = bodyParser.parse(message.payload, mode)

        val dscMessage = coseDecoder.decodeDscMessage(this)
        DccData(
            header = header,
            certificate = body.parsed.asCertificate,
            certificateJson = body.raw,
            kid = message.kid,
            dscMessage = dscMessage
        ).also {
            censor.addCertificateToCensor(it)
        }.also {
            Timber.v("Parsed covid certificate for %s", it.certificate.nameData.familyNameStandardized)
        }
    } catch (e: InvalidHealthCertificateException) {
        Timber.e(e)
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private val DccV1.asCertificate: DccV1.MetaData
        get() = when {
            isVaccinationCertificate -> asVaccinationCertificate!!
            isTestCertificate -> asTestCertificate!!
            isRecoveryCertificate -> asRecoveryCertificate!!
            else -> throw InvalidHealthCertificateException(HC_JSON_SCHEMA_INVALID)
        }
}

private const val PREFIX = "HC1:"

// Zip bomb
private const val DEFAULT_SIZE_LIMIT = 1024L * 1024 * 10L // 10 MB
