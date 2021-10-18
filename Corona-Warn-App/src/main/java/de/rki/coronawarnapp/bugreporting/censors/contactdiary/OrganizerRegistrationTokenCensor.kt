package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class OrganizerRegistrationTokenCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? = mutex.withLock {
        var container = CensorContainer(message)

        tan.forEach {
            container = container.censor(it, PLACEHOLDER + it.takeLast(3))
        }

        registrationRequestToCensor.forEach {
            container = container.censor(it.key, REQUEST_PLACEHOLDER + it.key.takeLast(4))
        }

        return container.nullIfEmpty()
    }

    companion object {
        private val mutex = Mutex()

        private val tan = mutableSetOf<String>()
        suspend fun addTan(tan: String) = mutex.withLock {
            this.tan.add(tan)
        }

        suspend fun clearTan() = mutex.withLock {
            tan.clear()
        }

        private val registrationRequestToCensor = mutableSetOf<RegistrationRequest>()
        suspend fun addRegistrationRequestToCensor(item: RegistrationRequest) = mutex.withLock {
            registrationRequestToCensor.add(item)
        }

        suspend fun clearRegistrationRequests() = mutex.withLock {
            registrationRequestToCensor.clear()
        }

        private const val PLACEHOLDER = "########-####-####-####-########"
        private const val REQUEST_PLACEHOLDER = "###-###-"
    }
}
