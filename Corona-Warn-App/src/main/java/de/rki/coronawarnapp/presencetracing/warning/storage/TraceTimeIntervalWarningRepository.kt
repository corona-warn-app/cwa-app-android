package de.rki.coronawarnapp.eventregistration.checkins.download

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.presencetracing.warning.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.flow.Flow
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import java.io.IOException

class TraceTimeIntervalWarningRepository(
    @AppContext private val context: Context
) {
    val allWarningPackages: Flow<List<TraceTimeIntervalWarningPackage>>
    get() {}
    val allPackageInfos: Flow<List<TraceTimeWarningPackageMetadata>>
    get() {}

    fun addWarningPackages(list: List<TraceTimeIntervalWarningPackage>) {}
    fun removeWarningPackages(list: List<TraceTimeIntervalWarningPackage>) {}

    fun markDownloadComplete(packageId: Long, eTag: String, path: File) {}
    fun deleteFile(path: File) {}
    fun deleteStalePackage(packageIds: List<Long>) {}
    fun createMetadata(packageId: Long): TraceTimeWarningPackageMetadata {}

    private val storageDir by lazy {
        File(context.cacheDir, "trace_warning_packages").apply {
            if (!exists()) {
                if (mkdirs()) {
                    Timber.d("Trace warning package directory created: %s", this)
                } else {
                    throw IOException("Trace warning package directory creation failed: $this")
                }
            }
        }
    }

    fun getFile(packageId: WarningPackageId): File = File(storageDir, "twp_$packageId.zip")

}

@Entity
data class TraceTimeWarningPackageMetadata(
    @PrimaryKey val packageId: Long,
    val createdAt: Instant,
    val eTag: String?,
    val absolutePath: String?,
    val isDownloadComplete: Boolean = false
)
