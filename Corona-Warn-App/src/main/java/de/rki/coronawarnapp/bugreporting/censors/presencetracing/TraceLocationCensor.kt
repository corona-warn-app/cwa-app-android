package de.rki.coronawarnapp.bugreporting.censors.presencetracing

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidDescription
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.presencetracing.locations.TraceLocationUserInput
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Censors Trace Location Data
 *
 * The information about which data to censor comes from two places
 * - traceLocationRepository, for traceLocations that are already stored
 * - dataToCensor, which is set before a traceLocation is created; this is needed in cases when the app crashes between
 * data input and storing
 */
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

        var newLogMsg = traceLocations.fold(entry.message) { initial, traceLocation ->
            var acc = initial

            acc = acc.replace(traceLocation.type.name, "TraceLocation#${traceLocation.id}/Type")

            withValidDescription(traceLocation.description) { description ->
                acc = acc.replace(description, "TraceLocation#${traceLocation.id}/Description")
            }

            withValidAddress(traceLocation.address) { address ->
                acc = acc.replace(address, "TraceLocation#${traceLocation.id}/Address")
            }

            acc
        }

        val inputDataToCensor = dataToCensor
        if (inputDataToCensor != null) {
            newLogMsg = newLogMsg
                .replace(inputDataToCensor.type.name, "TraceLocationUserInput#Type")
                .replace(inputDataToCensor.description, "TraceLocationUserInput#Description")
                .replace(inputDataToCensor.address, "TraceLocationUserInput#Address")
        }

        return entry.toNewLogLineIfDifferent(newLogMsg)
    }

    companion object {
        var dataToCensor: TraceLocationUserInput? = null
    }
}
