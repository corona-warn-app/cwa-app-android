package de.rki.coronawarnapp.covidcertificate.validation.core.validation.business

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class InvalidationProcessor @Inject constructor() {

    suspend fun process(
        invalidationRules: List<DccValidationRule>,
        validationClock: Instant,
        certificate: DccData<*>,
    ): Set<EvaluatedDccRule> = emptySet()
}
