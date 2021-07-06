package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.EvaluatedDccRule
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper.CertLogicEngineWrapper
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper.filterRelevantRules
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.wrapper.type
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class BusinessValidator @Inject constructor(
    private val certLogicEngineWrapper: CertLogicEngineWrapper,
    private val ruleRepository: DccValidationRepository,
) {
    suspend fun validate(
        arrivalCountry: DccCountry,
        validationClock: Instant,
        certificate: DccData<out DccV1.MetaData>,
    ): BusinessValidation {

        // accepted by arrival country
        val acceptanceResults = certLogicEngineWrapper.process(
            rules = ruleRepository.acceptanceRules(arrivalCountry).filterRelevantRules(
                validationClock = validationClock,
                certificateType = certificate.type
            ),
            validationClock = validationClock,
            certificate = certificate,
            countryCode = arrivalCountry.countryCode
        )

        // valid as defined by the issuing country
        val issuerCountry = DccCountry(certificate.header.issuer)
        val invalidationResults = certLogicEngineWrapper.process(
            rules = ruleRepository.invalidationRules(issuerCountry).filterRelevantRules(
                validationClock = validationClock,
                certificateType = certificate.type
            ),
            validationClock = validationClock,
            certificate = certificate,
            countryCode = issuerCountry.countryCode
        )

        return object : BusinessValidation {
            override val acceptanceRules: Set<EvaluatedDccRule> = acceptanceResults
            override val invalidationRules: Set<EvaluatedDccRule> = invalidationResults
        }
    }
}
