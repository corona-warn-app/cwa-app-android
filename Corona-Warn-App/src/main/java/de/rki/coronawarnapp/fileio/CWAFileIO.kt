package de.rki.coronawarnapp.fileio

import android.content.Context
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWAFileIO @Inject constructor(context: Context) {

    val rootDir = context.filesDir

    val exposureResultFile = File(rootDir, FILENAME_EXPOSURE_RESULT)

    companion object {
        private const val FILENAME_EXPOSURE_RESULT = "exposure_result.json"
    }
}
