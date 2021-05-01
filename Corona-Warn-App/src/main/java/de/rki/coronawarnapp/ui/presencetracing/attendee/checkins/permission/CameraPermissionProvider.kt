package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.permission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.permission.CameraSettings
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CameraPermissionProvider @Inject constructor(
    @AppContext private val context: Context,
    private val cameraSettings: CameraSettings
) {
    val deniedPermanently = cameraSettings
        .isCameraDeniedPermanently
        .flow
        .map { deniedPermanently ->
            deniedPermanently && !CameraPermissionHelper.hasCameraPermission(context)
        }

    fun checkSettings() {
        if (CameraPermissionHelper.hasCameraPermission(context)) {
            cameraSettings.isCameraDeniedPermanently.update { false }
        }
    }
}
