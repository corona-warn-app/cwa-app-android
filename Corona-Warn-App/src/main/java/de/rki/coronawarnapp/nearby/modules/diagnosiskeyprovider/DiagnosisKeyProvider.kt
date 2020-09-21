package de.rki.coronawarnapp.nearby.modules.diagnosiskeyprovider

import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import java.io.File

interface DiagnosisKeyProvider {

    /**
     * Takes an ExposureConfiguration object. Inserts a list of files that contain key
     * information into the on-device database. Provide the keys of confirmed cases retrieved
     * from your internet-accessible server to the Google Play service once requested from the
     * API. Information about the file format is in the Exposure Key Export File Format and
     * Verification document that is linked from google.com/covid19/exposurenotifications.
     *
     * @param keyFiles
     * @param configuration
     * @param token
     * @return
     */
    suspend fun provideDiagnosisKeys(
        keyFiles: Collection<File>, configuration: ExposureConfiguration?, token: String
    ): Boolean
}
