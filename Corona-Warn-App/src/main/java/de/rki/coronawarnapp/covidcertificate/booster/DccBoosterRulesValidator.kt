package de.rki.coronawarnapp.covidcertificate.booster

import androidx.annotation.VisibleForTesting
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import dagger.Lazy
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asEvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asExternalRule
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.toZonedDateTime
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
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
    suspend fun validateBoosterRules(dccList: List<CwaCovidCertificate>): DccValidationRule? {
        Timber.tag(TAG).d("validateBoosterRules(dccList=%s)", dccList)

        val boosterRules = boosterRulesRepository.updateBoosterNotificationRules()
        Timber.tag(TAG).d("boosterRules=%s", boosterRules)

        if (boosterRules.isEmpty()) {
            Timber.tag(TAG).d("Booster rules are empty")
            return null
        }

        // Find recent vaccination certificate
        val vaccinationCertificate = findRecentVaccinationCertificate(dccList)
        if (vaccinationCertificate == null) {
            Timber.tag(TAG).d("No vaccination certificate found")
            return null
        }
        Timber.tag(TAG).d("Most recent vaccination certificate=%s", vaccinationCertificate.certificateId)

        // Find recent recovery certificate
        val recoveryCertificate = findRecentRecoveryCertificate(dccList)
        Timber.tag(TAG).d("Most recent recovery certificate=%s", recoveryCertificate?.certificateId)

        val vacDccData = vaccinationCertificate.dccData
        val recDccData = recoveryCertificate?.dccData

        val payload = payload(vacDccData, recDccData)

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

        return ruleResults.firstOrNull { it.result == DccValidationRule.Result.PASSED }?.rule
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun payload(
        vacDccData: DccData<out DccV1.MetaData>,
        recDccData: DccData<out DccV1.MetaData>?
    ): String = try {
        val vacObjectNode = objectMapper.readTree(vacDccData.certificateJson) as ObjectNode
        val r0 = recDccData?.certificateJson?.let { objectMapper.readTree(it).path(R)[0] }
        r0?.let {
            Timber.tag(TAG).d("Setting r[0] to payload")
            vacObjectNode.putArray(R).add(it)
        }

        vacObjectNode.toString()
    } catch (e: Exception) {
        Timber.tag(TAG).d(e, "Setting  r[0] failed, fallback to Vaccine Json")
        vacDccData.certificateJson
    }

    companion object {
        private const val R = "r"
        private val TAG = tag<DccBoosterRulesValidator>()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun findRecentRecoveryCertificate(dccList: List<CwaCovidCertificate>): RecoveryCertificate? = dccList
    .filterIsInstance<RecoveryCertificate>()
    .maxWithOrNull(
        compareBy(
            { it.testedPositiveOn },
            { it.headerIssuedAt }
        )
    )

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun findRecentVaccinationCertificate(dccList: List<CwaCovidCertificate>): VaccinationCertificate? = dccList
    .filterIsInstance<VaccinationCertificate>()
    .maxWithOrNull(
        compareBy(
            { it.vaccinatedOn },
            { it.headerIssuedAt }
        )
    )
