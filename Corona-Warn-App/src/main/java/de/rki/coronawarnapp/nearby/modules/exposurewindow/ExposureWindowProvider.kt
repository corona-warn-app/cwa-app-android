package de.rki.coronawarnapp.nearby.modules.exposurewindow

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.ExposureWindow

interface ExposureWindowProvider {
    suspend fun exposureWindows(): List<ExposureWindow>
    suspend fun getDiagnosisKeysDataMapping(): DiagnosisKeysDataMapping
    suspend fun setDiagnosisKeysDataMapping(diagnosisKeysDataMapping: DiagnosisKeysDataMapping)
}
