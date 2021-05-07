package de.rki.coronawarnapp.vaccination.core.qrcode

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidVaccinationQRCodeException.ErrorCode.VC_NO_VACCINATION_ENTRY
import javax.inject.Inject

class VaccinationCertificateV1Decoder @Inject constructor() {

    private val keyEuDgcV1 = CBORObject.FromObject(1)
    private val keyHCert = CBORObject.FromObject(-260)

    fun decode(map: CBORObject): VaccinationCertificateV1 {
        try {
            var certificate: VaccinationCertificateV1? = null
            map[keyHCert]?.let { hcert ->
                hcert[keyEuDgcV1]?.let {
                    val json = it.ToJSONString()
                    certificate = Gson().fromJson<VaccinationCertificateV1>(json)
                }
            }
            return certificate!!.validate()
        } catch (e: InvalidVaccinationQRCodeException) {
            throw e
        } catch (e: Throwable) {
            throw InvalidVaccinationQRCodeException(HC_CBOR_DECODING_FAILED)
        }
    }

    private fun VaccinationCertificateV1.validate(): VaccinationCertificateV1 {
        if (vaccinationDatas.size < 1) {
            throw InvalidVaccinationQRCodeException(VC_NO_VACCINATION_ENTRY)
        }
        return this
    }
}
