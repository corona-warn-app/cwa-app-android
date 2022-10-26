package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import android.content.Context
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDscStorage @Inject constructor(
    @AppContext context: Context,
    private val dscDataParser: DscDataParser,
) {
    private val mutex = Mutex()
    private val dscDir = File(context.filesDir, "dsc_storage")
    private val dscFile = File(dscDir, "dsclist")

    suspend fun load(): DscSignatureList? = mutex.withLock {
        Timber.v("load()")

        if (dscFile.exists()) {
            return try {
                dscDataParser.parse(
                    rawData = dscFile.readBytes(),
                    updatedAt = Instant.ofEpochMilli(dscFile.lastModified())
                )
            } catch (exception: Exception) {
                Timber.e(exception, "Can't load cached data")
                null
            }
        } else {
            null
        }
    }

    suspend fun save(rawData: ByteArray): Unit = mutex.withLock {
        Timber.v("save (rawData.size=${rawData.size}")

        if (dscDir.mkdirs()) Timber.v("Parent folder created.")

        if (dscFile.exists()) {
            Timber.v("Overwriting ${dscFile.length()} from ${dscFile.lastModified()}")
        }

        try {
            dscFile.writeBytes(rawData)
        } catch (e: Exception) {
            Timber.e(e, "Failed to write data to file")
        }
    }

    suspend fun clear() = mutex.withLock {
        if (dscFile.exists() && dscFile.delete()) {
            Timber.v("Local dsc storage file deleted.")
        }
    }
}
