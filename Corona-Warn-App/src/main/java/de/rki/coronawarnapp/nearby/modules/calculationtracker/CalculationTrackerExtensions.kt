package de.rki.coronawarnapp.nearby.modules.calculationtracker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Instant

fun CalculationTracker.isCurrentlyCalculating(): Flow<Boolean> = calculations.map { snapshot ->
    snapshot.values.any { it.state == Calculation.State.CALCULATING }
}

fun CalculationTracker.latestFinishedCalculation(): Flow<Calculation?> =
    calculations.map { snapshot ->
        snapshot.values.maxByOrNull { it.finishedAt ?: Instant.EPOCH }
    }
