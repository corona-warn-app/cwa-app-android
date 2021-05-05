package de.rki.coronawarnapp.bugreporting.censors.presencetracing

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidAddress
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidDescription
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Reusable
class CheckInsCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    private val checkInRepository: CheckInRepository
) : BugCensor {

    private val checkInsFlow by lazy {
        checkInRepository.allCheckIns.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val checkIns = checkInsFlow.first()

        if (checkIns.isEmpty()) return null

        val newLogMsg = checkIns.fold(entry.message) { initial, checkIn ->

            var acc = initial

            withValidDescription(checkIn.description) { description ->
                acc = acc.replace(description, "CheckIn#${checkIn.id}/Description")
            }

            withValidAddress(checkIn.address) { address ->
                acc = acc.replace(address, "CheckIn#${checkIn.id}/Address")
            }

            acc
        }

        return entry.toNewLogLineIfDifferent(newLogMsg)
    }
}
