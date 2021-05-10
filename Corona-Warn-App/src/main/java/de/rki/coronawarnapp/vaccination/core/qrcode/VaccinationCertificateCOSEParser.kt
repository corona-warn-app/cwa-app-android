package de.rki.coronawarnapp.vaccination.core.qrcode

import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_COSE_MESSAGE_INVALID
import timber.log.Timber
import javax.inject.Inject

class VaccinationCertificateCOSEParser @Inject constructor(
    private val healthCertificateCOSEDecoder: HealthCertificateCOSEDecoder,
    private val vaccinationCertificateV1Parser: VaccinationCertificateV1Parser,
) {

    fun parse(rawCOSEObject: RawCOSEObject): VaccinationCertificateData {
        return rawCOSEObject
            .decodeCOSEObject()
            .decodeCBORObject()
    }

    private fun RawCOSEObject.decodeCOSEObject(): CBORObject {
        return try {
            healthCertificateCOSEDecoder.decode(this)
        } catch (e: InvalidHealthCertificateException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidHealthCertificateException(HC_COSE_MESSAGE_INVALID)
        }
    }

    private fun CBORObject.decodeCBORObject(): VaccinationCertificateData {
        return try {
            vaccinationCertificateV1Parser.decode(this)
        } catch (e: Exception) {
            Timber.e(e)
            throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
        }
    }
}
