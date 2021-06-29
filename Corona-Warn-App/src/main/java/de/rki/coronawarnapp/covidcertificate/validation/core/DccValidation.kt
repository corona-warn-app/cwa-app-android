package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.BusinessValidation
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.technical.TechnicalValidation

data class DccValidation(
    val technicalValidation: TechnicalValidation,
    val businessValidation: BusinessValidation,
) : TechnicalValidation by technicalValidation,
    BusinessValidation by businessValidation {

    val state: State
        get() = State.PASSED // TODO

    enum class State {
        PASSED,
        OPEN,
        TECHNICAL_FAILURE,
        FAILURE,
    }
}
