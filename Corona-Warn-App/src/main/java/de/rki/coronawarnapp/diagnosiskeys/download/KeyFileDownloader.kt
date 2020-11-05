package de.rki.coronawarnapp.diagnosiskeys.download

import dagger.Reusable
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import java.io.File
import javax.inject.Inject

/**
 * Downloads new or missing key files from the CDN
 */
@Reusable
class KeyFileDownloader @Inject constructor(
    private val keyFileSyncTool: KeyFileSyncTool
) {

    suspend fun asyncFetchKeyFiles(wantedCountries: List<LocationCode>): List<File> {
        return keyFileSyncTool.syncKeyFiles(wantedCountries).availableKeys.map { it.path }
    }
}
