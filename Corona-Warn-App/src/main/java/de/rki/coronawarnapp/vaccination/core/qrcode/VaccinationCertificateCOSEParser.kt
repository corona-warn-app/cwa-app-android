package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import timber.log.Timber
import javax.inject.Inject

class VaccinationCertificateCOSEParser @Inject constructor(
    private val healthCertificateCOSEDecoder: HealthCertificateCOSEDecoder,
    private val vaccinationCertificateV1Decoder: VaccinationCertificateV1Decoder,
) {

    fun parse(rawCOSEObject: RawCOSEObject): VaccinationCertificateData {
        val certificate = rawCOSEObject
            .extractCBORObject()
            .decodeCBORObject()

        return VaccinationCertificateData(
            vaccinationCertificate = certificate
        )
    }

    private fun RawCOSEObject.extractCBORObject(): CBORObject {
        return try {
            healthCertificateCOSEDecoder.decode(this)
        } catch (e: InvalidHealthCertificateException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
        }
    }

    private fun CBORObject.decodeCBORObject(): VaccinationCertificateV1 {
        return try {
            vaccinationCertificateV1Decoder.decode(this)
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
        }
    }
}
