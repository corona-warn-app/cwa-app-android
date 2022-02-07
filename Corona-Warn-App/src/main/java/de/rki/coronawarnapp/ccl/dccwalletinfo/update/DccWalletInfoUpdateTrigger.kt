package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterCertificateChange
import de.rki.coronawarnapp.ccl.dccwalletinfo.update.DccWalletInfoUpdateTask.DccWalletInfoUpdateTriggerType.TriggeredAfterConfigUpdate
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import timber.log.Timber
import javax.inject.Inject

class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController
) {

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

    fun triggerDccWalletInfoUpdateAfterCertificateChange() {
        Timber.tag(TAG).d("triggerDccWalletInfoUpdateAfterCertificateChange()")
        taskController.submit(
            DefaultTaskRequest(
                type = DccWalletInfoUpdateTask::class,
                arguments = DccWalletInfoUpdateTask.Arguments(TriggeredAfterCertificateChange),
                originTag = TAG
            )
        )
    }

    companion object {
        private val TAG = tag<DccWalletInfoUpdateTrigger>()
    }
}
