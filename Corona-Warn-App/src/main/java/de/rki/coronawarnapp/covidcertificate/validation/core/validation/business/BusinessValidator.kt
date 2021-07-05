package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.EvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper.CertLogicEngineWrapper
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class BusinessValidator @Inject constructor(
    private val certLogicEngineWrapper: CertLogicEngineWrapper,
    private val ruleRepository: DccValidationRuleRepository,
) {
    suspend fun validate(
        arrivalCountry: DccCountry,
        validationClock: Instant,
        certificate: DccData<*>,
    ): BusinessValidation {

        val acceptanceResults = certLogicEngineWrapper.process(
            rules = ruleRepository.acceptanceRules(arrivalCountry),
            validationClock = validationClock,
            certificate = certificate,
            arrivalCountry = arrivalCountry
        )

        val invalidationResults = certLogicEngineWrapper.process(
            rules = ruleRepository.invalidationRules(arrivalCountry),
            validationClock = validationClock,
            certificate = certificate,
            arrivalCountry = arrivalCountry
        )

        return object : BusinessValidation {
            override val acceptanceRules: Set<EvaluatedDccRule> = acceptanceResults
            override val invalidationRules: Set<EvaluatedDccRule> = invalidationResults
        }
    }
}
