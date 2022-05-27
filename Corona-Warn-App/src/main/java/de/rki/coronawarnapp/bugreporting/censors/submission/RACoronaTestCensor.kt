package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Reusable
class RACoronaTestCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    coronaTestRepository: CoronaTestRepository
) : BugCensor {

    private val mutex = Mutex()

    private val dayOfBirthFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // We keep a history of rat corona test so that we are able to censor even after they got deleted
    private val ratCoronaTestHistory = mutableSetOf<RACoronaTest>()

    init {
        coronaTestRepository
            .allCoronaTests
            .map { it.filterIsInstance<RACoronaTest>() }
            .onEach { mutex.withLock { ratCoronaTestHistory.addAll(it) } }
            .launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensorContainer? = mutex.withLock {

        var newMessage = CensorContainer(message)

        ratCoronaTestHistory.forEach { ratCoronaTest ->
            withValidName(ratCoronaTest.firstName) { firstName ->
                newMessage = newMessage.censor(firstName, "RATest/FirstName")
            }

            withValidName(ratCoronaTest.lastName) { lastName ->
                newMessage = newMessage.censor(lastName, "RATest/LastName")
            }

            ratCoronaTest.dateOfBirth?.format(dayOfBirthFormatter)?.let { dateOfBirthString ->
                newMessage = newMessage.censor(dateOfBirthString, "RATest/DateOfBirth")
            }
        }

        return newMessage.nullIfEmpty()
    }
}
