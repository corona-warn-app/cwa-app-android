package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Reusable
class RegistrationTokenCensor @Inject constructor(
    private val coronaTestRepository: CoronaTestRepository,
) : BugCensor {
    override suspend fun checkLog(entry: LogLine): LogLine? {
        val tokens = coronaTestRepository.coronaTests.first().map { it.registrationToken }

        if (tokens.isEmpty()) return null

        var newMessage = entry.message

        for (token in tokens) {
            if (!entry.message.contains(token)) continue

            newMessage = newMessage.replace(token, PLACEHOLDER + token.takeLast(4))
        }

        return entry.toNewLogLineIfDifferent(newMessage)
    }

    companion object {
        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
