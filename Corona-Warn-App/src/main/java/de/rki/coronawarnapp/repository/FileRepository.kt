package de.rki.coronawarnapp.repository

import com.google.gson.Gson
import de.rki.coronawarnapp.util.serialization.fromJson
import java.io.File
import java.io.FileWriter

open class FileRepository {

    inline fun <reified T> loadOrNull(file: File): T? {
        if (!file.exists()) return null

        return Gson().fromJson(file)
    }

    fun <T> writeOrDelete(t: T?, file: File) {
        if (t == null) {
            if (file.exists()) {
                file.delete()
            }
        } else {
            FileWriter(file).use {
                Gson().toJson(t, it)
            }
        }
    }
}
