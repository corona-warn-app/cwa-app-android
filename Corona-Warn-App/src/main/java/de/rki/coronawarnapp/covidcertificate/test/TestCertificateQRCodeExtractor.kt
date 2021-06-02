package de.rki.coronawarnapp.covidcertificate.test

import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.util.compression.deflate
import de.rki.coronawarnapp.util.compression.inflate
import de.rki.coronawarnapp.util.encoding.Base45Decoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidTestCertificateException
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestCertificateQRCodeExtractor @Inject constructor(
    private val coseDecoder: HealthCertificateCOSEDecoder,
    private val headerParser: HealthCertificateHeaderParser,
    private val bodyParser: TestCertificateDccParser,
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
            testCertificateData = rawCoseObject.decode(),
            qrCode = rawCoseObject.encode()
        )
    }

    /**
     * May throw an **[InvalidTestCertificateException]**
     */
    fun extract(qrCode: String) = TestCertificateQRCode(
        testCertificateData = qrCode.extract(),
        qrCode = qrCode
    )

    private fun RawCOSEObject.decrypt(decryptionKey: ByteArray): RawCOSEObject {
        return coseDecoder.decryptMessage(
            input = this,
            decryptionKey = decryptionKey
        )
    }

    private fun String.extract(): TestCertificateData =
        removePrefix(PREFIX)
            .decodeBase45()
            .decompress()
            .decode()

    private fun RawCOSEObject.encode(): String {
        return PREFIX + compress().encodeBase45()
    }

    private fun ByteArray.encodeBase45(): String = try {
        Base45Decoder.encode(this)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidTestCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun RawCOSEObject.compress(): ByteArray = try {
        this.deflate()
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidTestCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    private fun RawCOSEObject.decode(): TestCertificateData {
        val cbor = coseDecoder.decode(this)
        return cbor.parse()
    }

    private fun CBORObject.parse(): TestCertificateData {
        Timber.v("Parsing COSE for test certificate.")
        return TestCertificateData(
            header = headerParser.parse(this),
            certificate = bodyParser.parse(this)
        ).also {
            // todo CertificateQrCodeCensor.addCertificateToCensor(it)
        }.also {
            Timber.v("Parsed test certificate for %s", it.certificate.nameData.givenNameStandardized)
        }
    }

    private fun String.decodeBase45(): ByteArray = try {
        Base45Decoder.decode(this)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidTestCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun ByteArray.decompress(): RawCOSEObject = try {
        this.inflate(sizeLimit = DEFAULT_SIZE_LIMIT)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidTestCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    companion object {
        private const val PREFIX = "HC1:"

        // Zip bomb
        private const val DEFAULT_SIZE_LIMIT = 1024L * 1024 * 10L // 10 MB
    }
}
