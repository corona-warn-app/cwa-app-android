package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.ExposureWindow

interface ExposureWindowProvider {
    suspend fun exposureWindows(): List<ExposureWindow>
}
