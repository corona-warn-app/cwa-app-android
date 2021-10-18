package de.rki.coronawarnapp.bugreporting.censors.contactdiary

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import javax.inject.Inject

@Reusable
class OrganizerRegistrationTokenCensor @Inject constructor() : BugCensor {

    override suspend fun checkLog(message: String): CensorContainer? {
        var container = CensorContainer(message)

        synchronized(tan) {
            tan.forEach {
                container = container.censor(it, PLACEHOLDER + it.takeLast(3))
            }
        }

        synchronized(registrationRequestToCensor) {
            registrationRequestToCensor.forEach {
                container = container.censor(it.key, REQUEST_PLACEHOLDER + it.key.takeLast(4))
            }
        }

        return container.nullIfEmpty()
    }

    companion object {
        private val tan = mutableSetOf<String>()
        fun addTan(tan: String) = synchronized(tan) {
            this.tan.add(tan)
        }

        fun clearTan() = synchronized(tan) {
            tan.clear()
        }

        private val registrationRequestToCensor = mutableSetOf<RegistrationRequest>()
        fun addRegistrationRequestToCensor(item: RegistrationRequest) = synchronized(registrationRequestToCensor) {
            registrationRequestToCensor.add(item)
        }

        fun clearRegistrationRequests() = synchronized(registrationRequestToCensor) {
            registrationRequestToCensor.clear()
        }

        private const val PLACEHOLDER = "########-####-####-####-########"
        private const val REQUEST_PLACEHOLDER = "###-###-"
    }
}
