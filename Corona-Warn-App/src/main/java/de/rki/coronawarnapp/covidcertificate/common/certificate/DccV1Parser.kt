package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidVaccinationCertificateException
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccV1Parser @Inject constructor(
    @BaseGson private val gson: Gson
) {

    fun parse(map: CBORObject, mode: Mode): DccV1 = try {
        map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                this.toCertificate(mode)
            } ?: throw InvalidVaccinationCertificateException(ErrorCode.HC_CWT_NO_DGC)
        } ?: throw InvalidVaccinationCertificateException(ErrorCode.HC_CWT_NO_HCERT)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(ErrorCode.HC_CBOR_DECODING_FAILED, cause = e)
    }

    private fun CBORObject.toCertificate(mode: Mode): DccV1 = try {
        val json = ToJSONString()
        gson.fromJson<DccV1>(json).toValidated(mode)
    } catch (e: InvalidVaccinationCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidVaccinationCertificateException(ErrorCode.JSON_SCHEMA_INVALID)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun DccV1.toValidated(mode: Mode): DccV1 = this
        .run {
            when (mode) {
                Mode.CERT_VAC_STRICT -> if (vaccinations?.size == 1) return@run this
                else throw InvalidVaccinationCertificateException(
                    if (vaccinations.isNullOrEmpty()) ErrorCode.NO_VACCINATION_ENTRY
                    else ErrorCode.MULTIPLE_VACCINATION_ENTRIES
                )
                Mode.CERT_VAC_LENIENT -> {
                    Timber.w("Lenient: Vaccination data contained multiple entries.")
                    copy(vaccinations = listOf(vaccinations?.maxByOrNull { it.vaccinatedAt }!!))
                }
                Mode.CERT_SINGLE_STRICT -> if (vaccinations.isNullOrEmpty() || vaccinations.size == 1) return@run this
                else throw InvalidVaccinationCertificateException(ErrorCode.MULTIPLE_VACCINATION_ENTRIES)
                else -> if (vaccinations.isNullOrEmpty()) return@run this
                else throw InvalidVaccinationCertificateException(ErrorCode.MULTIPLE_VACCINATION_ENTRIES)
            }
        }
        .apply {
            // Apply otherwise we risk accidentally accessing the original obj in the outer scope
            // Force date parsing
            // check for non null (Gson does not enforce it) & force date parsing
            require(version.isNotBlank())
            require(nameData.familyNameStandardized.isNotBlank())
            dateOfBirth
            vaccinations?.forEach {
                it.vaccinatedAt
                require(it.certificateIssuer.isNotBlank())
                require(it.certificateCountry.isNotBlank())
                require(it.marketAuthorizationHolderId.isNotBlank())
                require(it.medicalProductId.isNotBlank())
                require(it.targetId.isNotBlank())
                require(it.doseNumber > 0)
                require(it.totalSeriesOfDoses > 0)
            }
            tests?.forEach {
                it.testResultAt
                it.sampleCollectedAt
                require(it.certificateIssuer.isNotBlank())
                require(it.certificateCountry.isNotBlank())
                require(it.targetId.isNotBlank())
                require(it.testCenter.isNotBlank())
                require(it.testResult.isNotBlank())
                require(it.testType.isNotBlank())
            }
            recoveries?.forEach {
                // TODO
            }
        }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }

    enum class Mode {
        CERT_VAC_STRICT, // exactly one vaccination certificate allowed
        CERT_VAC_LENIENT,  // multiple vaccination certificates allowed
        CERT_REC_STRICT, // exactly one recovery certificate allowed
        CERT_TEST_STRICT, // exactly one test certificate allowed
        CERT_SINGLE_STRICT // exactly one certificate allowed
    }
}
