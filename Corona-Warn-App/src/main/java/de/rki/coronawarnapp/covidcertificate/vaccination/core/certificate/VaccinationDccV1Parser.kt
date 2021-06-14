package de.rki.coronawarnapp.covidcertificate.vaccination.core.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject

@Reusable
class VaccinationDccV1Parser @Inject constructor(
    @BaseGson private val gson: Gson
) {

    fun parse(map: CBORObject, lenient: Boolean): VaccinationDccV1 = try {
        map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                this.toCertificate(lenient = lenient)
            } ?: throw InvalidVaccinationCertificateException(ErrorCode.HC_CWT_NO_DGC)
        } ?: throw InvalidVaccinationCertificateException(ErrorCode.HC_CWT_NO_HCERT)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(ErrorCode.HC_CBOR_DECODING_FAILED, cause = e)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun VaccinationDccV1.toValidated(lenient: Boolean): VaccinationDccV1 = this
        .run {
            if (payloads.isEmpty()) throw InvalidVaccinationCertificateException(ErrorCode.VC_NO_VACCINATION_ENTRY)

            if (payloads.size == 1) return@run this

            if (lenient) {
                Timber.w("Lenient: Vaccination data contained multiple entries.")
                copy(payloads = listOf(payloads.maxByOrNull { it.vaccinatedAt }!!))
            } else {
                throw InvalidVaccinationCertificateException(ErrorCode.VC_MULTIPLE_VACCINATION_ENTRIES)
            }
        }
        .apply {
            // Apply otherwise we risk accidentally accessing the original obj in the outer scope
            // Force date parsing
            // check for non null (Gson does not enforce it) & force date parsing
            require(version.isNotBlank())
            require(nameData.familyNameStandardized.isNotBlank())
            dateOfBirth
            payload.let {
                it.vaccinatedAt
                require(it.certificateIssuer.isNotBlank())
                require(it.certificateCountry.isNotBlank())
                require(it.marketAuthorizationHolderId.isNotBlank())
                require(it.medicalProductId.isNotBlank())
                require(it.targetId.isNotBlank())
                require(it.doseNumber > 0)
                require(it.totalSeriesOfDoses > 0)
            }
        }

    private fun CBORObject.toCertificate(lenient: Boolean): VaccinationDccV1 = try {
        val json = ToJSONString()
        gson.fromJson<VaccinationDccV1>(json).toValidated(lenient = lenient)
    } catch (e: InvalidVaccinationCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(ErrorCode.JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
