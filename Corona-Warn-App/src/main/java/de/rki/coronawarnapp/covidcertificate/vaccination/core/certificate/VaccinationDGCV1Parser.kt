package de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate

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
        map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidVaccinationCertificateException(HC_CWT_NO_DGC)
        } ?: throw InvalidVaccinationCertificateException(HC_CWT_NO_HCERT)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(HC_CBOR_DECODING_FAILED)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun VaccinationDGCV1.validate(): VaccinationDGCV1 {
        if (vaccinationDatas.isNullOrEmpty()) {
            throw InvalidVaccinationCertificateException(VC_NO_VACCINATION_ENTRY)
        }
        // check for non null (Gson does not enforce it) & force date parsing
        version!!
        nameData.familyNameStandardized.isNotBlank()
        dateOfBirth
        vaccinationDatas.forEach {
            it.vaccinatedAt
            it.certificateIssuer.isNotBlank()
            it.countryOfVaccination.isNotBlank()
            it.marketAuthorizationHolderId.isNotBlank()
            it.medicalProductId.isNotBlank()
            it.targetId.isNotBlank()
            it.doseNumber > 0
            it.totalSeriesOfDoses > 0
        }
        return this
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<VaccinationDGCV1>(json).validate()
    } catch (e: InvalidVaccinationCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
