package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryCoronaTestEntity
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class CoronaTestCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    coronaTestRepository: CoronaTestRepository,
    contactDiaryRepository: ContactDiaryRepository
) : BugCensor {

    private val mutex = Mutex()

    // Keep a history to have references even after the user deletes a test
    private val tokenHistory = mutableSetOf<String>()
    private val identifierHistory = mutableSetOf<String>()

    init {
        listOf(
            contactDiaryRepository.testResults,
            coronaTestRepository.allCoronaTests,
        ).merge()
            .filterNotNull()
            .onEach { tests ->
                mutex.withLock {
                    tests.forEach { test ->
                        when (test) {
                            is BaseCoronaTest -> {
                                // The Registration Token is received after registration of PCR and RAT tests. It is required to poll the test result.
                                tokenHistory.add(test.registrationToken)
                                identifierHistory.add(test.identifier)
                            }
                            is ContactDiaryCoronaTestEntity -> {
                                // Test ids stay in the contact diary DB even after test is removed from the device
                                tokenHistory.add(test.id)
                            }
                        }
                    }
                }
            }.launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensorContainer? = mutex.withLock {

        var newMessage = CensorContainer(message)

        for (token in tokenHistory) {
            if (!message.contains(token)) continue

            newMessage = newMessage.censor(token, PLACEHOLDER + token.takeLast(4))
        }

        identifierHistory
            .filter { message.contains(it) }
            .forEach {
                newMessage = newMessage.censor(it, "${it.take(11)}CoronaTest/Identifier")
            }

        return newMessage.nullIfEmpty()
    }

    companion object {
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
