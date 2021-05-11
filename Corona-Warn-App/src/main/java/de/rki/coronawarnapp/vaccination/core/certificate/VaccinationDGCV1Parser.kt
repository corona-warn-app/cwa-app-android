package de.rki.coronawarnapp.vaccination.core.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import javax.inject.Inject

class VaccinationDGCV1Parser @Inject constructor() {

    fun parse(map: CBORObject): VaccinationDGCV1 = try {
        var certificate: VaccinationDGCV1? = null
        map[keyHCert]?.let { hcert ->
            hcert[keyEuDgcV1]?.let {
                val json = it.ToJSONString()
                certificate = Gson().fromJson<VaccinationDGCV1>(json)
            }
        }

        certificate!!.validate()
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun VaccinationDGCV1.validate(): VaccinationDGCV1 {
        if (vaccinationDatas.isEmpty()) {
            throw InvalidHealthCertificateException(VC_NO_VACCINATION_ENTRY)
        }
        dateOfBirth
        vaccinationDatas.forEach {
            it.vaccinatedAt
        }
        return this
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
