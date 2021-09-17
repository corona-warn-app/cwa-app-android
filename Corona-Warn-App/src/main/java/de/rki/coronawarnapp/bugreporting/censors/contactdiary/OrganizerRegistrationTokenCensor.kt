package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class OrganizerRegistrationTokenCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): BugCensor.CensorContainer? = mutex.withLock {
        if (registrationToken.isEmpty()) return null

        var container = BugCensor.CensorContainer(message)

        registrationToken.forEach {
            container = container.censor(it, PLACEHOLDER + it.takeLast(3))
        }

        return container.nullIfEmpty()
    }

    companion object {
        private val mutex = Mutex()
        private val registrationToken = mutableSetOf<String>()
        suspend fun addRegistrationToken(tan: String) = mutex.withLock {
            registrationToken.add(tan)
        }

        suspend fun clearRegistrationTokens() = mutex.withLock {
            registrationToken.clear()
        }

        private const val PLACEHOLDER = "########-####-####-####-########"
    }
}
