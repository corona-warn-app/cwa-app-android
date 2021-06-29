package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class BusinessValidator @Inject constructor() {

    suspend fun validate(
        arrivalCountries: Set<DccCountry>, // For future allow multiple country selection
        validationClock: Instant,
        certificate: DccData<*>,
    ): BusinessValidation = object : BusinessValidation {
        // TODO
        override val acceptanceRules: Set<EvaluatedDccRule> = emptySet()
        override val invalidationRules: Set<EvaluatedDccRule> = emptySet()
    }
}
