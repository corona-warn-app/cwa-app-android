package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.permission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.permission.CameraPermissionHelper
import de.rki.coronawarnapp.util.permission.CameraPermissionSettings
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CameraPermissionProvider @Inject constructor(
    @AppContext private val context: Context,
    private val cameraPermissionSettings: CameraPermissionSettings
) {
    val deniedPermanently = cameraPermissionSettings
        .isCameraDeniedPermanently
        .flow
        .map { deniedPermanently ->
            deniedPermanently &&
                !CameraPermissionHelper.hasCameraPermission(context)
        }
}
