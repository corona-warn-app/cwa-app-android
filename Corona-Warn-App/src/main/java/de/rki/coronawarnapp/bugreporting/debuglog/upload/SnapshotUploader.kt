package de.rki.coronawarnapp.bugreporting.debuglog.upload

import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.LogUploadServer
import de.rki.coronawarnapp.bugreporting.debuglog.upload.server.auth.LogUploadAuthServer
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
