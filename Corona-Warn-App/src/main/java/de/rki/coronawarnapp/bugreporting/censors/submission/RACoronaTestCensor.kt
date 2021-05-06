package de.rki.coronawarnapp.bugreporting.censors.submission

import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.withValidName
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.joda.time.format.DateTimeFormat
import javax.inject.Inject

class RACoronaTestCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    private val coronaTestRepository: CoronaTestRepository
) : BugCensor {

    private val dayOfBirthFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    private val coronaTestFlow by lazy {
        coronaTestRepository.coronaTests.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {

        val raCoronaTestFlow = coronaTestFlow.map { tests -> tests.filterIsInstance<RACoronaTest>() }.first()
        val raCoronaTest = raCoronaTestFlow.firstOrNull() ?: return null

        var newMessage = entry.message

        with(raCoronaTest) {
            withValidName(firstName) { firstName ->
                newMessage = newMessage.replace(firstName, "RATest/FirstName")
            }

            withValidName(lastName) { lastName ->
                newMessage = newMessage.replace(lastName, "RATest/LastName")
            }

            val dateOfBirthString = dateOfBirth?.toString(dayOfBirthFormatter) ?: return@with

            newMessage = newMessage.replace(dateOfBirthString, "RATest/DateOfBirth")
        }

        return entry.toNewLogLineIfDifferent(newMessage)
    }
}
