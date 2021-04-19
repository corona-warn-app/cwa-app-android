package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidDescription
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Reusable
class TraceLocationCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    private val traceLocationRepository: TraceLocationRepository
) : BugCensor {

    private val traceLocationsFlow by lazy {
        traceLocationRepository.allTraceLocations.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val traceLocations = traceLocationsFlow.first()

        if (traceLocations.isEmpty()) return null

        val newLogMsg = traceLocations.fold(entry.message) { initial, traceLocation ->
            var acc = initial

            withValidDescription(traceLocation.description) { description ->
                acc = acc.replace(description, "TraceLocation#${traceLocation.id}/Description")
            }

            withValidAddress(traceLocation.address) { address ->
                acc = acc.replace(address, "TraceLocation#${traceLocation.id}/Address")
            }

            acc
        }

        return entry.toNewLogLineIfDifferent(newLogMsg)
    }
}
