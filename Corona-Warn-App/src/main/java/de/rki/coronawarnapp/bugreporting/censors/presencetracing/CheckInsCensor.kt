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

    override suspend fun checkLog(message: String): CensoredString? {

        val checkIns = checkInsFlow.first()

        if (checkIns.isEmpty()) return null

        val newLogMsg = checkIns.fold(CensoredString(message)) { initial, checkIn ->

            var acc = initial

            withValidDescription(checkIn.description) { description ->
                acc += acc.censor(description, "CheckIn#${checkIn.id}/Description")
            }

            withValidAddress(checkIn.address) { address ->
                acc += acc.censor(address, "CheckIn#${checkIn.id}/Address")
            }

            acc
        }

        return newLogMsg.toNullIfUnmodified()
    }
}
