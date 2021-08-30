package de.rki.coronawarnapp.covidcertificate.booster

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asEvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asExternalRule
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.toZonedDateTime
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccBoosterRulesValidator @Inject constructor(
    private val boosterRulesRepository: BoosterRulesRepository,
    private val engine: Lazy<DefaultCertLogicEngine>,
    @BaseJackson private val objectMapper: ObjectMapper,
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun validateBoosterRules(dccList: List<CwaCovidCertificate>): EvaluatedDccRule? {
        Timber.tag(TAG).d("validateBoosterRules(dccList=%s)", dccList)

        val boosterRules = boosterRulesRepository.rules.first()
        Timber.tag(TAG).d("boosterRules=%s", boosterRules)

        if (boosterRules.isEmpty()) {
            Timber.tag(TAG).d("Booster rules are empty")
            return null
        }

        // Find recent vaccination certificate
        val recentVaccinationCertificate = findRecentVaccinationCertificate(dccList)
        if (recentVaccinationCertificate == null) {
            Timber.tag(TAG).d("No vaccination certificate found")
            return null
        }
        Timber.tag(TAG).d("Most recent vaccination certificate=%s", recentVaccinationCertificate)

        // Find recent recovery certificate
        val recentRecoveryCertificate = findRecentRecoveryCertificate(dccList)
        Timber.tag(TAG).d("Most recent recovery certificate=%s", recentRecoveryCertificate)

        val vacDccData = recentVaccinationCertificate.dccData
        val recDccData = recentRecoveryCertificate?.dccData

        val payload = objectMapper.writeValueAsString(
            JsonPayload(
                v = listOf(vacDccData.certificate.payload),
                r = recDccData?.certificate?.payload?.let { listOf(it) },
                nam = vacDccData.certificate.nameData,
                ver = vacDccData.certificate.version
            )
        )

        val externalParameter = ExternalParameter(
            validationClock = ZonedDateTime.now(UTC_ZONE_ID),
            valueSets = emptyMap(),
            countryCode = DccCountry.DE,
            issuerCountryCode = DccCountry.DE,
            exp = vacDccData.header.expiresAt.toZonedDateTime(UTC_ZONE_ID),
            iat = vacDccData.header.issuedAt.toZonedDateTime(UTC_ZONE_ID),
            kid = "",
            region = ""
        )

        val ruleResults = engine.get().validate(
            certificateType = CertificateType.VACCINATION,
            hcertVersionString = vacDccData.certificate.version,
            rules = boosterRules.map { it.asExternalRule },
            externalParameter = externalParameter,
            payload = payload,
        ).map { result ->
            result.validationErrors?.forEach {
                Timber.tag(TAG).e(it, "Errors during validation of %s", result.rule.identifier)
            }
            result.asEvaluatedDccRule
        }.also { evaluated ->
            Timber.tag(TAG).i("Evaluated rules are:")
            evaluated.forEach {
                Timber.tag(TAG).i("Rule %s %s has resulted in %s.", it.rule.identifier, it.rule.version, it.result)
            }
        }.toSet()

        return ruleResults.firstOrNull { it.result == DccValidationRule.Result.PASSED }
    }

    companion object {
        private val TAG = DccBoosterRulesValidator::class.simpleName
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private data class JsonPayload(
        @JsonProperty("v") val v: List<DccV1.Payload>,
        @JsonProperty("r") val r: List<DccV1.Payload>?,
        @JsonProperty("nam") val nam: DccV1.NameData,
        @JsonProperty("ver") val ver: String,
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun findRecentRecoveryCertificate(dccList: List<CwaCovidCertificate>): RecoveryCertificate? {
    val recCertificates = dccList.filterIsInstance<RecoveryCertificate>()
    val recentFirstResultOnCert = recCertificates.maxByOrNull { it.testedPositiveOn }
    return recCertificates
        .filter { it.testedPositiveOn == recentFirstResultOnCert?.testedPositiveOn }
        .maxByOrNull { it.headerIssuedAt }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun findRecentVaccinationCertificate(dccList: List<CwaCovidCertificate>): VaccinationCertificate? {
    val vacCertificates = dccList.filterIsInstance<VaccinationCertificate>()
    val recentVaccinatedOnCert = vacCertificates.maxByOrNull { it.vaccinatedOn }
    return vacCertificates
        .filter { it.vaccinatedOn == recentVaccinatedOnCert?.vaccinatedOn }
        .maxByOrNull { it.headerIssuedAt }
}
