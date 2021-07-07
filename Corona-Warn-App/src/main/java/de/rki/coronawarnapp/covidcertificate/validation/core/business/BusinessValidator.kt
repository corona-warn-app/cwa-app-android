package de.rki.coronawarnapp.covidcertificate.validation.core.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidationRepository
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.CertLogicEngineWrapper
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.filterRelevantRules
import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.type
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class BusinessValidator @Inject constructor(
    private val certLogicEngineWrapper: CertLogicEngineWrapper,
    private val ruleRepository: DccValidationRepository,
    private val dccJsonSchema: DccJsonSchema,
) {
    suspend fun validate(
        arrivalCountry: DccCountry,
        validationClock: Instant,
        certificate: DccData<out DccV1.MetaData>,
    ): BusinessValidation {

        // TODO update value sets

        ruleRepository.refresh()

        // accepted by arrival country
        val acceptanceResults = certLogicEngineWrapper.process(
            rules = ruleRepository.acceptanceRules(arrivalCountry).filterRelevantRules(
                validationClock = validationClock,
                certificateType = certificate.type
            ),
            validationClock = validationClock,
            certificate = certificate,
            countryCode = arrivalCountry.countryCode,
            schemaJson = dccJsonSchema.rawSchema
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
            countryCode = issuerCountry.countryCode,
            schemaJson = dccJsonSchema.rawSchema
        )

        return BusinessValidation(
            acceptanceRules = acceptanceResults,
            invalidationRules = invalidationResults
        )
    }
}
