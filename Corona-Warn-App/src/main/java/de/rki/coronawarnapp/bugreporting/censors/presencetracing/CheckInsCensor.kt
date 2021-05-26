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
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class CheckInsCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    checkInRepository: CheckInRepository
) : BugCensor {

    private val mutex = Mutex()

    private val checkInsHistory = mutableSetOf<CheckIn>()

    init {
        checkInRepository.allCheckIns
            .onEach { mutex.withLock { checkInsHistory.addAll(it) } }
            .launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensoredString? = mutex.withLock {

        if (checkInsHistory.isEmpty()) return null

        val newLogMsg = checkInsHistory.fold(CensoredString.fromOriginal(message)) { initial, checkIn ->

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
