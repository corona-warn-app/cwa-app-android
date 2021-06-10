package de.rki.coronawarnapp.covidcertificate.test.core.qrcode

import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
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
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.covidcertificate.test.core.certificate.TestDccParser
import de.rki.coronawarnapp.covidcertificate.test.core.certificate.TestDccV1
import de.rki.coronawarnapp.util.compression.deflate
import de.rki.coronawarnapp.util.compression.inflate
import de.rki.coronawarnapp.util.encoding.Base45Decoder
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestCertificateQRCodeExtractor @Inject constructor(
    private val coseDecoder: DccCoseDecoder,
    private val headerParser: DccHeaderParser,
    private val bodyParser: TestDccParser,
) {

    /**
     * May throw an **[InvalidTestCertificateException]**
     */
    fun extract(
        decryptionKey: ByteArray,
        rawCoseObjectEncrypted: ByteArray,
    ): TestCertificateQRCode {
        val rawCoseObject = rawCoseObjectEncrypted.decrypt(decryptionKey)
        return TestCertificateQRCode(
            data = rawCoseObject.decode(),
            qrCode = rawCoseObject.encode()
        )
    }

    /**
     * May throw an **[InvalidTestCertificateException]**
     */
    fun extract(qrCode: String) = TestCertificateQRCode(
        data = qrCode.extract(),
        qrCode = qrCode
    )

    private fun RawCOSEObject.decrypt(decryptionKey: ByteArray): RawCOSEObject = try {
        coseDecoder.decryptMessage(
            input = this,
            decryptionKey = decryptionKey
        )
    } catch (e: InvalidHealthCertificateException) {
        throw InvalidTestCertificateException(e.errorCode)
    } catch (e: Throwable) {
        Timber.e(e, HC_COSE_MESSAGE_INVALID.toString())
        throw InvalidTestCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun String.extract(): DccData<TestDccV1> =
        removePrefix(PREFIX)
            .decodeBase45()
            .decompress()
            .decode()

    private fun RawCOSEObject.encode(): String {
        return PREFIX + compress().encodeBase45()
    }

    private fun RawCOSEObject.decode(): DccData<TestDccV1> = try {
        coseDecoder.decode(this).parse()
    } catch (e: InvalidHealthCertificateException) {
        throw InvalidTestCertificateException(e.errorCode)
    } catch (e: Throwable) {
        Timber.e(e, HC_COSE_MESSAGE_INVALID.toString())
        throw InvalidTestCertificateException(HC_COSE_MESSAGE_INVALID)
    }

    private fun CBORObject.parse(): DccData<TestDccV1> = try {
        DccData(
            header = headerParser.parse(this),
            certificate = bodyParser.parse(this)
        ).also {
            Timber.v("Parsed test certificate for %s", it.certificate.nameData.givenNameStandardized)
        }
    } catch (e: InvalidHealthCertificateException) {
        throw InvalidTestCertificateException(e.errorCode)
    } catch (e: Throwable) {
        Timber.e(e, HC_CBOR_DECODING_FAILED.toString())
        throw InvalidTestCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun String.decodeBase45(): ByteArray = try {
        Base45Decoder.decode(this)
    } catch (e: Throwable) {
        Timber.e(e, HC_BASE45_DECODING_FAILED.toString())
        throw InvalidTestCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun ByteArray.encodeBase45(): String = try {
        Base45Decoder.encode(this)
    } catch (e: Throwable) {
        Timber.e(e, HC_BASE45_ENCODING_FAILED.toString())
        throw InvalidTestCertificateException(HC_BASE45_ENCODING_FAILED)
    }

    private fun RawCOSEObject.compress(): ByteArray = try {
        this.deflate()
    } catch (e: Throwable) {
        Timber.e(e, HC_ZLIB_COMPRESSION_FAILED.toString())
        throw InvalidTestCertificateException(HC_ZLIB_COMPRESSION_FAILED)
    }

    private fun ByteArray.decompress(): RawCOSEObject = try {
        this.inflate(sizeLimit = DEFAULT_SIZE_LIMIT)
    } catch (e: Throwable) {
        Timber.e(e, HC_ZLIB_DECOMPRESSION_FAILED.toString())
        throw InvalidTestCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    companion object {
        private const val PREFIX = "HC1:"

        // Zip bomb
        private const val DEFAULT_SIZE_LIMIT = 1024L * 1024 * 10L // 10 MB
    }
}
