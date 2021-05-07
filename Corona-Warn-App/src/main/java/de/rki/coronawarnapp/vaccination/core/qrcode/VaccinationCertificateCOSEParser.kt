package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_COSE_MESSAGE_INVALID
import de.rki.coronawarnapp.vaccination.decoder.COSEDecoder
import timber.log.Timber
import javax.inject.Inject

class VaccinationCertificateCOSEParser @Inject constructor(
    private val COSEDecoder: COSEDecoder,
    private val VaccinationCertificateV1Decoder: VaccinationCertificateV1Decoder,
) {

    fun parse(rawCOSEObject: RawCOSEObject): VaccinationCertificateData {
        val certificate = rawCOSEObject
            .extractCBORObject()
            .decodeCBORObject()

        return VaccinationCertificateData(
            vaccinationCertificate = certificate
        )
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
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidVaccinationQRCodeException(HC_CBOR_DECODING_FAILED)
        }
    }
}
