package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.bugreporting.censors.vaccination.DccQrCodeCensor
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_REC_STRICT
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser.Mode.CERT_SINGLE_STRICT
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
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_COMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
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
     * May throw an **[InvalidHealthCertificateException]**
     */
    override fun extract(rawString: String): DccQrCode {
        val mode = CERT_SINGLE_STRICT
        return extract(rawString, mode)
    }

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun extractEncrypted(
        decryptionKey: ByteArray,
        rawCoseObjectEncrypted: ByteArray,
    ): DccQrCode {
        val qrCodeString = rawCoseObjectEncrypted.decrypt(decryptionKey).encode()
        return extract(qrCodeString)
    }

    /**
     * May throw an **[InvalidHealthCertificateException]**
     */
    fun extract(rawString: String, mode: DccV1Parser.Mode): DccQrCode {
        DccQrCodeCensor.addQRCodeStringToCensor(rawString)

        return try {
            val parsedData = rawString
                .removePrefix(PREFIX)
                .decodeBase45()
                .decompress()
                .parse(mode)

            toDccQrCode(rawString, parsedData).also {
                when (mode) {
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
            when (mode) {
                CERT_VAC_STRICT, CERT_VAC_LENIENT ->
                    throw InvalidVaccinationCertificateException(e.errorCode)
                CERT_REC_STRICT ->
                    throw InvalidRecoveryCertificateException(e.errorCode)
                CERT_TEST_STRICT ->
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
                    parsedData.header,
                    parsedData.certificate
                ),
            )
            is TestDccV1 -> TestCertificateQRCode(
                qrCode = rawString,
                data = DccData(
                    parsedData.header,
                    parsedData.certificate
                ),
            )
            is RecoveryDccV1 -> RecoveryCertificateQRCode(
                qrCode = rawString,
                data = DccData(
                    parsedData.header,
                    parsedData.certificate
                ),
            )
            else -> throw InvalidHealthCertificateException(JSON_SCHEMA_INVALID)
        }

    private fun String.decodeBase45(): ByteArray = try {
        Base45Decoder.decode(this)
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
        Timber.v("Parsing COSE for covid certificate.")
        val cbor = coseDecoder.decode(this)
        DccData(
            header = headerParser.parse(cbor),
            certificate = bodyParser.parse(cbor, mode).toCertificate
        ).also {
            DccQrCodeCensor.addCertificateToCensor(it)
        }.also {
            Timber.v("Parsed covid certificate for %s", it.certificate.nameData.familyNameStandardized)
        }
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private val DccV1.isVaccinationCertificate: Boolean
        get() = this.vaccinations?.isNotEmpty() == true

    private val DccV1.isTestCertificate: Boolean
        get() = this.tests?.isNotEmpty() == true

    private val DccV1.isRecoveryCertificate: Boolean
        get() = this.recoveries?.isNotEmpty() == true

    private val DccV1.toCertificate: DccV1.MetaData
        get() = when {
            isVaccinationCertificate -> VaccinationDccV1(
                version = version,
                nameData = nameData,
                dateOfBirth = dateOfBirth,
                personIdentifier = personIdentifier,
                vaccination = vaccinations!!.first()
            )
            isTestCertificate -> TestDccV1(
                version = version,
                nameData = nameData,
                dateOfBirth = dateOfBirth,
                personIdentifier = personIdentifier,
                test = tests!!.first()
            )
            isRecoveryCertificate -> RecoveryDccV1(
                version = version,
                nameData = nameData,
                dateOfBirth = dateOfBirth,
                personIdentifier = personIdentifier,
                recovery = recoveries!!.first()
            )
            else -> throw InvalidHealthCertificateException(JSON_SCHEMA_INVALID)
        }
}

private const val PREFIX = "HC1:"
// Zip bomb
private const val DEFAULT_SIZE_LIMIT = 1024L * 1024 * 10L // 10 MB
