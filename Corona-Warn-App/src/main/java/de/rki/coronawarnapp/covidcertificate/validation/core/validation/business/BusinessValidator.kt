package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.serialization.SerializationModule
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import dgca.verifier.app.engine.UTC_ZONE_ID
import dgca.verifier.app.engine.data.CertificateType
import dgca.verifier.app.engine.data.Rule
import dgca.verifier.app.engine.data.Type
import org.joda.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject

@Reusable
class BusinessValidator @Inject constructor(
    private val acceptanceProcessor: AcceptanceProcessor,
    private val invalidationProcessor: InvalidationProcessor,
    private val ruleRepository: DccValidationRepository,
) {

    suspend fun validate(
        arrivalCountries: Set<DccCountry>,
        validationClock: Instant,
        certificate: DccData<*>,
    ): BusinessValidation {
        // TODO Update repository?
        val country = arrivalCountries.first()

        val jsonLogicValidator = DefaultJsonLogicValidator()
        val engine = DefaultCertLogicEngine(jsonLogicValidator)
        val rule = Rule(
            identifier = "identifier",
            type = Type.ACCEPTANCE,
            version = "1.0.0",
            schemaVersion = "1.0.0",
            engine = "engine",
            engineVersion = "1.0.0",
            certificateType = CertificateType.GENERAL,
            descriptions = emptyMap(),
            validFrom = ZonedDateTime.now().withZoneSameInstant(UTC_ZONE_ID),
            validTo = ZonedDateTime.now().withZoneSameInstant(UTC_ZONE_ID),
            affectedString = emptyList(),
            logic = SerializationModule.jacksonBaseMapper.createObjectNode(),
            countryCode = "de",
            region = null
        )

        val acceptanceResults = acceptanceProcessor.process(
            acceptanceRules = ruleRepository.acceptanceRules(country),
            validationClock = validationClock,
            certificate = certificate,
        )
        val invalidationResults = invalidationProcessor.process(
            invalidationRules = ruleRepository.invalidationRules(country),
            validationClock = validationClock,
            certificate = certificate,
        )

        return object : BusinessValidation {
            // TODO
            override val acceptanceRules: Set<EvaluatedDccRule> = acceptanceResults
            override val invalidationRules: Set<EvaluatedDccRule> = invalidationResults
        }
    }
}
