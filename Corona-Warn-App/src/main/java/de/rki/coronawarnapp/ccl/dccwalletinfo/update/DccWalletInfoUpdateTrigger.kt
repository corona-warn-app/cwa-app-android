package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterCertificateChange
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterConfigUpdate
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController,
    personCertificateProvider: PersonCertificatesProvider,
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider
) {

    init {
        personCertificateProvider.personCertificates
            .onStart { Timber.tag(TAG).d("Observing certificates for changes") }
            .distinctUntilChanged { oldCerts, newCerts -> oldCerts.sortedQrCodeHashSet == newCerts.sortedQrCodeHashSet }
            .onEach {
                Timber.tag(TAG).d("Certificates changed!")
                triggerDccWalletInfoUpdateAfterCertificateChange()
            }
            .catch { Timber.tag(TAG).e(it, "Failed to observe certificates for changes") }
            .launchIn(scope = appScope + dispatcherProvider.IO)
    }

    fun triggerDccWalletInfoUpdateAfterConfigUpdate(configurationChanged: Boolean = false) {
        Timber.tag(TAG).d("triggerDccWalletInfoUpdateAfterConfigUpdate()")
        taskController.submit(
            DefaultTaskRequest(
                type = DccWalletInfoUpdateTask::class,
                arguments = DccWalletInfoUpdateTask.Arguments(
                    TriggeredAfterConfigUpdate(
                        configurationChanged
                    )
                ),
                originTag = TAG
            )
        )
    }

    private fun triggerDccWalletInfoUpdateAfterCertificateChange() {
        Timber.tag(TAG).d("triggerDccWalletInfoUpdateAfterCertificateChange()")
        taskController.submit(
            DefaultTaskRequest(
                type = DccWalletInfoUpdateTask::class,
                arguments = DccWalletInfoUpdateTask.Arguments(TriggeredAfterCertificateChange),
                originTag = TAG
            )
        )
    }

    private val Set<PersonCertificates>.sortedQrCodeHashSet: Set<String>
        get() = flatMap { personCert ->
            personCert.certificates.map { it.qrCodeHash }
        }
            .sorted()
            .toSet()

    companion object {
        private val TAG = tag<DccWalletInfoUpdateTrigger>()
    }
}
