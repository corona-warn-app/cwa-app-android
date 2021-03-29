package de.rki.coronawarnapp.util.permission

import android.content.Context
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraPermissionSettings @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("camera.permission.settings", Context.MODE_PRIVATE)
    }

    val isCameraDeniedPermanently = prefs.createFlowPreference(
        key = PREFS_KEY_CAMERA_PERMISSION_DENIED,
        defaultValue = false
    )

    companion object {
        private const val PREFS_KEY_CAMERA_PERMISSION_DENIED = "isCameraDeniedPermanently"
    }
}
