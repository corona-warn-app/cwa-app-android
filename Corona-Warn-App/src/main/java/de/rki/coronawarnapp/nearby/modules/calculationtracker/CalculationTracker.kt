package de.rki.coronawarnapp.nearby.modules.calculationtracker

import kotlinx.coroutines.flow.Flow

interface CalculationTracker {
    val calculations: Flow<Map<String, Calculation>>

    fun trackNewCalaculation(identifier: String)

    fun finishCalculation(identifier: String, result: Calculation.Result)
}
