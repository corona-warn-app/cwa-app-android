package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import android.content.Context
import de.rki.coronawarnapp.util.CameraPermissionHelper
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CameraPermissionProvider @Inject constructor(
    @AppContext private val context: Context
) {
    val permissionGranted = flow {
        while (true) {
            emit(
                CameraPermissionHelper.hasCameraPermission(context)
            )
            delay(1_000L)
        }
    }
}
