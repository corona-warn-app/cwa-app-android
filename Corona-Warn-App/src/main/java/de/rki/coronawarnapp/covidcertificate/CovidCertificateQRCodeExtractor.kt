package de.rki.coronawarnapp.covidcertificate

import de.rki.coronawarnapp.util.compression.inflate
import de.rki.coronawarnapp.util.encoding.Base45Decoder
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateCOSEDecoder
import de.rki.coronawarnapp.vaccination.core.certificate.HealthCertificateHeaderParser
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_BASE45_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_ZLIB_DECOMPRESSION_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.RawCOSEObject
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateData
import okio.ByteString
import timber.log.Timber
import javax.inject.Inject

class CovidCertificateExtractor @Inject constructor(
    private val aesEncryptor: AesEncryptor,
    private val coseDecoder: HealthCertificateCOSEDecoder,
    private val headerParser: HealthCertificateHeaderParser,
    private val bodyParser: TestCertificateDccParser,
) {
    fun extract(encryptedCOSEObject: ByteArray, decryptionKey: ByteArray) {
        val cbor = coseDecoder.decode(encryptedCOSEObject)
        val header = headerParser.parse(cbor)
        val encryptedPayload = bodyParser.parse(cbor)
        aesEncryptor.decrypt(
            encryptedData = encryptedPayload,
            dek = decryptionKey
        )
    }

    private fun ByteArray.encodeBase45(): String = try {
        Base45Decoder.encode(this)
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_BASE45_DECODING_FAILED)
    }

    private fun RawCOSEObject.compress(): ByteArray = try {
        this.inflate()
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidHealthCertificateException(HC_ZLIB_DECOMPRESSION_FAILED)
    }

    fun RawCOSEObject.parse(): VaccinationCertificateData {
        Timber.v("Parsing COSE for vaccination certificate.")
        val cbor = coseDecoder.decode(this)

        return VaccinationCertificateData(
            header = headerParser.parse(cbor),
            certificate = bodyParser.parse(cbor)
        ).also {
            // todo CertificateQrCodeCensor.addCertificateToCensor(it)
        }
    }

    companion object {
        private const val PREFIX = "HC1:"
    }

    data class DecryptionKey(
        val encryptedDEK: ByteString,
        val privateKey: RSAKey.Private
    )
}
