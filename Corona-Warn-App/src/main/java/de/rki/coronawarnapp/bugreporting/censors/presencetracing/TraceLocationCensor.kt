package de.rki.coronawarnapp.bugreporting.censors.presencetracing

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidDescription
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

    override suspend fun checkLog(message: String): CensoredString? {

        val traceLocations = traceLocationsFlow.first()

        var newLogMsg = traceLocations.fold(CensoredString(message)) { initial, traceLocation ->
            var acc = initial

            acc += acc.censor(traceLocation.type.name, "TraceLocation#${traceLocation.id}/Type")

            withValidDescription(traceLocation.description) { description ->
                acc += acc.censor(description, "TraceLocation#${traceLocation.id}/Description")
            }

            withValidAddress(traceLocation.address) { address ->
                acc += acc.censor(address, "TraceLocation#${traceLocation.id}/Address")
            }

            acc
        }

        val inputDataToCensor = dataToCensor
        if (inputDataToCensor != null) {
            newLogMsg += newLogMsg.censor(inputDataToCensor.type.name, "TraceLocationUserInput#Type")

            withValidDescription(inputDataToCensor.description) {
                newLogMsg += newLogMsg.censor(inputDataToCensor.description, "TraceLocationUserInput#Description")
            }

            withValidAddress(inputDataToCensor.address) {
                newLogMsg += newLogMsg.censor(inputDataToCensor.address, "TraceLocationUserInput#Address")
            }
        }

        return newLogMsg.toNullIfUnmodified()
    }

    companion object {
        var dataToCensor: TraceLocationUserInput? = null
    }
}
