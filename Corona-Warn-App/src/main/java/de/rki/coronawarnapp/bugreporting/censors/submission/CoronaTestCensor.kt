package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensoredString
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class CoronaTestCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    coronaTestRepository: CoronaTestRepository,
) : BugCensor {

    private val mutex = Mutex()

    // Keep a history to have references even after the user deletes a test
    private val tokenHistory = mutableSetOf<String>()
    private val identifierHistory = mutableSetOf<String>()

    init {
        coronaTestRepository.coronaTests
            .filterNotNull()
            .onEach { tests ->
                mutex.withLock {
                    // The Registration Token is received after registration of PCR and RAT tests. It is required to poll the test result.
                    tokenHistory.addAll(tests.map { it.registrationToken })
                    identifierHistory.addAll(tests.map { it.identifier })
                }
            }.launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensoredString? = mutex.withLock {

        var newMessage = CensoredString.fromOriginal(message)

        for (token in tokenHistory) {
            if (!message.contains(token)) continue

            newMessage += newMessage.censor(token, PLACEHOLDER + token.takeLast(4))
        }

        identifierHistory
            .filter { message.contains(it) }
            .forEach {
                newMessage += newMessage.censor(it, "${it.take(11)}CoronaTest/Identifier")
            }

        return newMessage.toNullIfUnmodified()
    }

    companion object {
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
