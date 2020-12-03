package de.rki.coronawarnapp.test.api.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.risk.RiskLevelTask
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.task.common.DefaultTaskRequest
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.smartLiveData
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class TestForApiFragmentViewModel @AssistedInject constructor(
    @AppContext private val context: Context,
    private val taskController: TaskController,
    private val tracingPermissionHelper: TracingPermissionHelper
) : CWAViewModel() {

    val errorEvents = SingleLiveEvent<Throwable>()
    val infoEvent = SingleLiveEvent<String>()
    val permissionRequiredEvent = SingleLiveEvent<(Activity) -> Unit>()

    init {
        tracingPermissionHelper.callback = object : TracingPermissionHelper.Callback {
            override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                infoEvent.postValue("isTracingEnabled: $isTracingEnabled")
            }

            override fun onError(error: Throwable) {
                errorEvents.postValue(error)
            }
        }
    }

    fun calculateRiskLevelClicked() {
        taskController.submit(DefaultTaskRequest(RiskLevelTask::class, originTag = "TestForApiFragmentViewModel"))
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tracingPermissionHelper.handleActivityResult(requestCode, resultCode, data)
    }

    fun requestTracingPermission() {
        tracingPermissionHelper.startTracing { permissionRequest ->
            permissionRequiredEvent.postValue(permissionRequest)
        }
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
