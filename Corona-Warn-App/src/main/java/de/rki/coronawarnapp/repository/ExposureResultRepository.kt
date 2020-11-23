package de.rki.coronawarnapp.repository

import com.google.gson.Gson
import de.rki.coronawarnapp.fileio.CWAFileIO
import de.rki.coronawarnapp.risk.ExposureResult
import de.rki.coronawarnapp.util.serialization.fromJson
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureResultRepository @Inject constructor(fileio: CWAFileIO) {

    private val file = fileio.exposureResultFile

    fun load(): ExposureResult? {
        if (!file.exists()) return null

        return Gson().fromJson(file)
    }

    fun upsert(exposureResult: ExposureResult?) {
        if (exposureResult == null) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            FileWriter(file).use {
                Gson().toJson(exposureResult, it)
            }
        }
    }
}
