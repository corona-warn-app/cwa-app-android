package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import org.joda.time.Instant
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
