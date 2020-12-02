package de.rki.coronawarnapp.nearby.modules.diagnosiskeysdatamapper

import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping

interface DiagnosisKeysDataMapper {
    suspend fun updateDiagnosisKeysDataMapping(newDiagnosisKeysDataMapping: DiagnosisKeysDataMapping)
}
