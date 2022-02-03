package de.rki.coronawarnapp.ccl.dccwalletinfo.update

import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import javax.inject.Inject

class DccWalletInfoUpdateTrigger @Inject constructor(
    private val taskController: TaskController
) {

    fun triggerDccWalletInfoUpdate(configurationChanged: Boolean = false) {
        taskController.submit(
            DefaultTaskRequest(
                DccWalletInfoUpdateTask::class,
                arguments = DccWalletInfoUpdateTask.Arguments(configurationChanged),
                originTag = TAG
            )
        )
    }

    companion object {
        private val TAG = tag<DccWalletInfoUpdateTrigger>()
    }
}
