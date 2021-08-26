package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class BoosterRulesRepository {
    // TODO
    val rules: Flow<List<DccValidationRule>> = emptyFlow()
}
