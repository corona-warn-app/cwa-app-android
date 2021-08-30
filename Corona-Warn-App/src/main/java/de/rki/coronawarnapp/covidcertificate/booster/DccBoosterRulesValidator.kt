package de.rki.coronawarnapp.covidcertificate.booster

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.ValueSetWrapper
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asEvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asExternalRule
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.asZonedDateTime
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.toZonedDateTime
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.util.serialization.BaseJackson
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.ExternalParameter
import kotlinx.coroutines.flow.first
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccBoosterRulesValidator @Inject constructor(
    private val boosterRulesRepository: BoosterRulesRepository,
    private val dccJsonSchema: DccJsonSchema,
    private val valueSetWrapper: ValueSetWrapper,
    @BaseJackson private val objectMapper: ObjectMapper,
) {

    private val engine: DefaultCertLogicEngine by lazy {
        DefaultCertLogicEngine(
            DefaultAffectedFieldsDataRetriever(
                schemaJsonNode = objectMapper.readTree(dccJsonSchema.rawSchema),
                objectMapper = objectMapper
            ),
            DefaultJsonLogicValidator()
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun validateBoosterRules(dccList: List<CwaCovidCertificate>): EvaluatedDccRule? {
        Timber.tag(TAG).d("validateBoosterRules(dccList=%s)", dccList)

        val boosterRules = boosterRulesRepository.rules.first()
        Timber.tag(TAG).d("boosterRules=%s", boosterRules)

        // Find recent vaccination certificate
        val vacCertificates = dccList.filterIsInstance<VaccinationCertificate>()
        val recentVaccinatedOnCert = vacCertificates.maxByOrNull { it.vaccinatedOn }
        val recentVaccinationCertificate = vacCertificates
            .filter { it.vaccinatedOn == recentVaccinatedOnCert?.vaccinatedOn }
            .maxByOrNull { it.headerIssuedAt }

        if (recentVaccinationCertificate == null) {
            Timber.tag(TAG).d("No vaccination certificate found")
            return null
        }
        Timber.tag(TAG).d("Most recent vaccination certificate=%s", recentVaccinationCertificate)

        // Find recent recovery certificate
        val recCertificates = dccList.filterIsInstance<RecoveryCertificate>()
        val recentFirstResultOnCert = recCertificates.maxByOrNull { it.testedPositiveOn }
        val recentRecoveryCertificate = recCertificates
            .filter { it.testedPositiveOn == recentFirstResultOnCert?.testedPositiveOn }
            .maxByOrNull { it.headerIssuedAt }
        Timber.tag(TAG).d("Most recent recovery certificate=%s", recentRecoveryCertificate)
        val vacDccData = recentVaccinationCertificate.dccData
        val recDccData = recentRecoveryCertificate?.dccData

        val externalParameter = ExternalParameter(
            kid = "",
            validationClock = DateTime.now(DateTimeZone.UTC).asZonedDateTime(UTC_ZONE_ID),
            valueSets = valueSetWrapper.valueMap.first(),
            countryCode = DccCountry.DE,
            issuerCountryCode = DccCountry.DE,
            exp = vacDccData.header.expiresAt.toZonedDateTime(UTC_ZONE_ID),
            iat = vacDccData.header.issuedAt.toZonedDateTime(UTC_ZONE_ID),
            region = ""
        )

        val payload = objectMapper.writeValueAsString(
            JsonPayload(
                v = listOf(vacDccData.certificate.payload),
                r = recDccData?.certificate?.payload?.let { listOf(it) },
                nam = vacDccData.certificate.nameData,
                ver = vacDccData.certificate.version
            )
        )

        val ruleResults = engine.validate(
            hcertVersionString = vacDccData.certificate.version,
            rules = boosterRules.map { it.asExternalRule },
            externalParameter = externalParameter,
            payload = payload,
            certificateType = CertificateType.VACCINATION
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

    private data class JsonPayload(
        @SerializedName("v") val v: List<DccV1.Payload>,
        @SerializedName("r") val r: List<DccV1.Payload>?,
        @SerializedName("nam") val nam: DccV1.NameData,
        @SerializedName("ver") val ver: String,
    )
}
