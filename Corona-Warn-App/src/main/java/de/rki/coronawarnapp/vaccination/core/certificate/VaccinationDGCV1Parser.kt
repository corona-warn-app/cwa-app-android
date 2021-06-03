package de.rki.coronawarnapp.vaccination.core.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_DGC
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_HCERT
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import de.rki.coronawarnapp.covidcertificate.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import javax.inject.Inject

@Reusable
class VaccinationDGCV1Parser @Inject constructor(
    @BaseGson private val gson: Gson
) {

    fun parse(map: CBORObject): VaccinationDGCV1 = try {
        val certificate: VaccinationDGCV1 = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidVaccinationCertificateException(HC_CWT_NO_DGC)
        } ?: throw InvalidVaccinationCertificateException(HC_CWT_NO_HCERT)

        certificate.validate()
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun VaccinationDGCV1.validate(): VaccinationDGCV1 {
        if (vaccinationDatas.isEmpty()) {
            throw InvalidVaccinationCertificateException(VC_NO_VACCINATION_ENTRY)
        }
        // Force date parsing
        dateOfBirth
        vaccinationDatas.forEach {
            it.vaccinatedAt
        }
        return this
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<VaccinationDGCV1>(json)
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
