package de.rki.coronawarnapp.test.api.ui

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.smartLiveData
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestForApiFragmentViewModel @AssistedInject constructor(
    @AppContext private val context: Context,
    private val taskController: TaskController
) : CWAViewModel() {

    fun calculateRiskLevelClicked() {
        taskController.submit(DefaultTaskRequest(RiskLevelTask::class, originTag = "TestForApiFragmentViewModel"))
    }

    val gmsState by smartLiveData {
        GoogleServicesState(
            version = PackageInfoCompat.getLongVersionCode(
                context.packageManager.getPackageInfo(
                    GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                    0
                )
            )
        )
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<TestForApiFragmentViewModel>
}
