package de.rki.coronawarnapp.nearby.modules.calculationtracker

import kotlinx.coroutines.flow.Flow

interface CalculationTracker {
    val calculations: Flow<Map<String, Calculation>>

    fun trackNewCalaculation(token: String)

    fun finishCalculation(token: String, result: Calculation.Result)
}
