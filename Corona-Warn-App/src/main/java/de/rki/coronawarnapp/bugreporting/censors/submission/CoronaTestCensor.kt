package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class CoronaTestCensor @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
) : BugCensor {

    // Keep a history to have references even after the user deletes a test
    private val tokenHistory = mutableSetOf<String>()
    private val identifierHistory = mutableSetOf<String>()

    override suspend fun checkLog(entry: LogLine): LogLine? {

        // The Registration Token is received after registration of PCR and RAT tests. It is required to poll the test result.
        val tokens = coronaTestRepository.coronaTests.first().map { it.registrationToken }
        tokenHistory.addAll(tokens)

        val identifiers = coronaTestRepository.coronaTests.first().map { it.identifier }
        identifierHistory.addAll(identifiers)

        var newMessage = entry.message
        for (token in tokenHistory) {
            if (!entry.message.contains(token)) continue

            newMessage = newMessage.replace(token, PLACEHOLDER + token.takeLast(4))
        }

        identifierHistory
            .filter { entry.message.contains(it) }
            .forEach {
                newMessage = newMessage.replace(it, "${it.take(11)}CoronaTest/Identifier")
            }

        return entry.toNewLogLineIfDifferent(newMessage)
    }

    companion object {
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
