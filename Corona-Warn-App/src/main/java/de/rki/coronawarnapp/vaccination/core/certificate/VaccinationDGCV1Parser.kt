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
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_MULTIPLE_VACCINATION_ENTRIES
import de.rki.coronawarnapp.vaccination.core.certificate.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import timber.log.Timber
import javax.inject.Inject

@Reusable
class VaccinationDGCV1Parser @Inject constructor(
    @BaseGson private val gson: Gson
) {

    fun parse(map: CBORObject, lenient: Boolean): VaccinationDGCV1 = try {
        val certificate: VaccinationDGCV1 = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_DGC)
        } ?: throw InvalidHealthCertificateException(VC_HC_CWT_NO_HCERT)

        certificate.toValidated(lenient)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
    }

    private fun VaccinationDGCV1.toValidated(lenient: Boolean): VaccinationDGCV1 = this
        .run {
            if (vaccinationDatas.isEmpty()) throw InvalidHealthCertificateException(VC_NO_VACCINATION_ENTRY)

            if (vaccinationDatas.size == 1) return@run this

            if (lenient) {
                Timber.w("Lenient: Vaccination data contained multiple entries.")
                copy(vaccinationDatas = listOf(vaccinationDatas.maxByOrNull { it.vaccinatedAt }!!))
            } else {
                throw InvalidHealthCertificateException(VC_MULTIPLE_VACCINATION_ENTRIES)
            }
        }
        .also {
            // Force date parsing
            dateOfBirth
            vaccinationDatas.forEach {
                it.vaccinatedAt
            }
        }

    private fun CBORObject.toCertificate(): VaccinationDGCV1 = try {
        val json = ToJSONString()
        gson.fromJson(json)
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(VC_JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
