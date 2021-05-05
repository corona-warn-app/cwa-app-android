package de.rki.coronawarnapp.vaccination.core.qrcode

import com.google.gson.Gson
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.qrcode.QrCodeExtractor
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject

class VaccinationQRCodeExtractor @Inject constructor(
    private val base45Decoder: Base45Decoder,
    private val zlibDecompressor: ZlibDecompressor,
    private val coseDecoder: CoseDecoder,
    private val CBORDecoder: CBORDecoder,
) : QrCodeExtractor<VaccinationCertificateQRCode> {

    override fun canHandle(rawString: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun extract(rawString: String): VaccinationCertificateQRCode {

//        val encoded = contextIdentifierService.decode(input, verificationResult)
//        val compressed = base45Service.decode(encoded, verificationResult)
//        val cose = compressorService.decode(compressed, verificationResult)
//        val cbor = coseService.decode(cose, verificationResult)
//        return cborService.decode(cbor, verificationResult)

        val cbor = rawString
            .decodeBase45()
            .decompress()
            .extractCosePayload()
            .decodeCBOR()
        cbor.payload.utf8()
            .extractData()
            .validate()
        return VaccinationCertificateQRCode(
            // Vaccine or prophylaxis
            qrCodeOriginalBase45 = rawString,
            qrCodeOriginalCBOR = cbor.payload,
        )
    }

    // step1 : decode base45
    private fun String.decodeBase45(): ByteArray {
        return try {
            base45Decoder.decode(this)
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidQRCodeException("Unsupported encoding. Supported encoding is base45.")
        }
    }

    // step 2: decompress with zlib

    private fun ByteArray.decompress(): ByteArray {
        return zlibDecompressor.decode(this)
    }

    // step 3: extract COSE payload
    private fun ByteArray.extractCosePayload(): ByteArray {
        return coseDecoder.decode(this)
    }

    // step 4
    private fun ByteArray.decodeCBOR(): CborDecoderResult {
        return CBORDecoder.decode(this)
    }

    // step 5: Unpack json
    private fun String.extractData(): VaccinationCertificateV1 {
        try {
            return Gson().fromJson(this)
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidQRCodeException("Malformed payload.")
        }
    }

    private fun VaccinationCertificateV1.validate() {
    }

    enum class ErrorCode {
        HC_BASE45_DECODING_FAILED,
        HC_ZLIB_DECOMPRESSION_FAILED,
        HC_COSE_TAG_INVALID,
        HC_COSE_MESSAGE_INVALID
    }
}
