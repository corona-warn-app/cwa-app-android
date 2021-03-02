package de.rki.coronawarnapp.bugreporting.logupload

import de.rki.coronawarnapp.bugreporting.logupload.auth.LogUploadAuthServer
import de.rki.coronawarnapp.bugreporting.logupload.server.LogUploadServer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotUploader @Inject constructor(
    private val uploadServer: LogUploadServer,
    private val authServer: LogUploadAuthServer
) {

    // TODO needs classes from different PR
    suspend fun uploadSnapshot(): Any {
        return Any()
    }
}
