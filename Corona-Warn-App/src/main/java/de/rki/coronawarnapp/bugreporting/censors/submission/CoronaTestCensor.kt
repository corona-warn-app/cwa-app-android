package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.util.CWADebug
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class CoronaTestCensor @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
) : BugCensor {

    override suspend fun checkLog(entry: LogLine): LogLine? {

        // The Registration Token is received after registration of PCR and RAT tests. It is required to poll the test result.
        val tokens = coronaTestRepository.coronaTests.first().map { it.registrationToken }
        val identifiers = coronaTestRepository.coronaTests.first().map { it.identifier }

        if (tokens.isEmpty()) return null

        var newMessage = entry.message

        for (token in tokens) {
            if (!entry.message.contains(token)) continue

            newMessage = if (CWADebug.isDeviceForTestersBuild) {
                newMessage.replace(token, PLACEHOLDER_TESTER + token.takeLast(27))
            } else {
                newMessage.replace(token, PLACEHOLDER + token.takeLast(4))
            }
        }

        identifiers
            .filter { entry.message.contains(it) }
            .forEach {
                newMessage = newMessage.replace(it, "${it.take(11)}CoronaTest/Identifier")
            }

        return entry.toNewLogLineIfDifferent(newMessage)
    }

    companion object {
        private const val PLACEHOLDER_TESTER = "########-"
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
