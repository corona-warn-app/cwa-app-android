package de.rki.coronawarnapp.vaccination.core.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_DGC
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_HC_CWT_NO_HCERT
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import javax.inject.Inject

@Reusable
class VaccinationDGCV1Parser @Inject constructor(
    @BaseGson private val gson: Gson
) {

    fun parse(map: CBORObject): VaccinationDGCV1 = try {
        val certificate: VaccinationDGCV1 = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_DGC)
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_HCERT)

        certificate.validate()
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
            // Force date parsing
            it.vaccinatedAt
        }
        return this
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<VaccinationDGCV1>(json)
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(VC_JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
