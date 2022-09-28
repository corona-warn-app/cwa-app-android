package de.rki.coronawarnapp.tracing

sealed class RiskCalculationState {

    object Idle : RiskCalculationState() {
        override fun toString(): String = "RiskCalculationState.Idle"
    }

    object Downloading : RiskCalculationState() {
        override fun toString(): String = "RiskCalculationState.Downloading"
    }

    object IsCalculating : RiskCalculationState() {
        override fun toString(): String = "RiskCalculationState.IsCalculating"
    }
}
