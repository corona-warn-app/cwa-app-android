package de.rki.coronawarnapp.bugreporting.censors.submission

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.censors.BugCensor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.CensorContainer
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@Reusable
class CoronaTestCertificateCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    coronaTestRepository: TestCertificateRepository,
) : BugCensor {

    private val mutex = Mutex()

    // Keep a history to have references even after the user deletes a test
    private val tokenHistory = mutableSetOf<String>()
    private val identifierHistory = mutableSetOf<String>()

    init {
        coronaTestRepository.certificates
            .filterNotNull()
            .onEach { cert ->
                mutex.withLock {
                    tokenHistory.addAll(cert.mapNotNull { it.registrationToken })
                    identifierHistory.addAll(cert.map { it.containerId.qrCodeHash })
                }
            }.launchIn(debugScope)
    }

    override suspend fun checkLog(message: String): CensorContainer? = mutex.withLock {

        var newMessage = CensorContainer(message)

        tokenHistory
            .filter { message.contains(it) }
            .forEach {
                newMessage = newMessage.censor(it, PLACEHOLDER + it.takeLast(4))
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
