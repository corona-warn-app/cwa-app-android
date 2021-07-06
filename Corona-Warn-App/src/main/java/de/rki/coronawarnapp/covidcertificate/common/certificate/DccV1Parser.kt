package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidRecoveryCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidTestCertificateException
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
                this.toCertificate().toValidated(mode)
            } ?: throw InvalidHealthCertificateException(ErrorCode.HC_CWT_NO_DGC)
        } ?: throw InvalidHealthCertificateException(ErrorCode.HC_CWT_NO_HCERT)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(ErrorCode.HC_CBOR_DECODING_FAILED, cause = e)
    }

    private fun CBORObject.toCertificate(): DccV1 = try {
        val json = ToJSONString()
        gson.fromJson(json)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(ErrorCode.HC_JSON_SCHEMA_INVALID)
    }

    private fun DccV1.toValidated(mode: Mode): DccV1 = try {
        checkModeRestrictions(mode)
            .apply {
                // Apply otherwise we risk accidentally accessing the original obj in the outer scope
                require(isSingleCertificate())
                checkFields()
            }
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(ErrorCode.HC_JSON_SCHEMA_INVALID)
    }

    private fun DccV1.checkModeRestrictions(mode: Mode) = when (mode) {
        Mode.CERT_VAC_STRICT ->
            if (vaccinations?.size != 1)
                throw InvalidVaccinationCertificateException(
                    if (vaccinations.isNullOrEmpty()) ErrorCode.NO_VACCINATION_ENTRY
                    else ErrorCode.MULTIPLE_VACCINATION_ENTRIES
                )
            else this
        Mode.CERT_VAC_LENIENT -> {
            if (vaccinations.isNullOrEmpty())
                throw InvalidVaccinationCertificateException(ErrorCode.NO_VACCINATION_ENTRY)
            Timber.w("Lenient: Vaccination data contained multiple entries.")
            copy(vaccinations = listOf(vaccinations.maxByOrNull { it.vaccinatedOn }!!))
        }
        Mode.CERT_REC_STRICT ->
            if (recoveries?.size != 1)
                throw InvalidRecoveryCertificateException(
                    if (recoveries.isNullOrEmpty()) ErrorCode.NO_RECOVERY_ENTRY
                    else ErrorCode.MULTIPLE_RECOVERY_ENTRIES
                )
            else this
        Mode.CERT_TEST_STRICT ->
            if (tests?.size != 1)
                throw InvalidTestCertificateException(
                    if (tests.isNullOrEmpty()) ErrorCode.NO_TEST_ENTRY
                    else ErrorCode.MULTIPLE_TEST_ENTRIES
                )
            else this
        else -> this
    }

    private fun DccV1.isSingleCertificate(): Boolean {
        return (vaccinations?.size ?: 0) + (tests?.size ?: 0) + (recoveries?.size ?: 0) == 1
    }

    private fun DccV1.checkFields() {
        // check for non null (Gson does not enforce it) + not blank & force date parsing
        require(version.isNotBlank())
        require(nameData.familyNameStandardized.isNotBlank())
        dateOfBirthFormatted
        vaccinations?.forEach {
            it.vaccinatedOnFormatted
            it.vaccinatedOn
            require(it.certificateIssuer.isNotBlank())
            require(it.certificateCountry.isNotBlank())
            require(it.marketAuthorizationHolderId.isNotBlank())
            require(it.medicalProductId.isNotBlank())
            require(it.targetId.isNotBlank())
            require(it.doseNumber > 0)
            require(it.totalSeriesOfDoses > 0)
        }
        tests?.forEach {
            it.sampleCollectedAt
            require(it.certificateIssuer.isNotBlank())
            require(it.certificateCountry.isNotBlank())
            require(it.targetId.isNotBlank())
            require(it.testResult.isNotBlank())
            require(it.testType.isNotBlank())
        }
        recoveries?.forEach {
            it.testedPositiveOnFormatted
            it.validFromFormatted
            it.validUntilFormatted
            it.validFrom
            it.validUntil
            require(it.certificateIssuer.isNotBlank())
            require(it.certificateCountry.isNotBlank())
            require(it.targetId.isNotBlank())
        }
    }

    enum class Mode {
        CERT_VAC_STRICT, // exactly one vaccination certificate allowed
        CERT_VAC_LENIENT, // multiple vaccination certificates allowed
        CERT_REC_STRICT, // exactly one recovery certificate allowed
        CERT_TEST_STRICT, // exactly one test certificate allowed
        CERT_SINGLE_STRICT; // exactly one certificate allowed
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
