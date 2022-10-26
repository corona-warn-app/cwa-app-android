package de.rki.coronawarnapp.bugreporting.debuglog.internal

import android.content.Context
import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.DebugLogger
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.files.Zipper
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.format.DateTimeFormatter
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@Reusable
class LogSnapshotter @Inject constructor(
    @AppContext private val context: Context,
    private val debugLogger: DebugLogger,
    private val timeStamper: TimeStamper
) {

    private val snapshotDir = File(context.cacheDir, "debuglog_snapshots")

    /**
     * Use **[Snapshot#delete]** after you are done.
     * Otherwise it will be deleted when another snapshot is taken.
     * @return a snapshot of the current debug log, or null if there was no log
     */
    fun snapshot(): Snapshot {
        Timber.tag(TAG).d("snapshot()")

        snapshotDir.listFiles()?.forEach {
            if (it.delete()) Timber.tag(TAG).w("Deleted stale snapshot: %s", it)
        }

        val now = timeStamper.nowUTC.toLocalDateTimeUserTz()
        // Avoid ":" in filename since it is a reserved character in Microsoft Windows
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss.SSS")
        val formattedFileName = "CWA Log ${now.format(formatter)}"
        if (!snapshotDir.exists() && snapshotDir.mkdirs()) {
            Timber.tag(TAG).v("Created %s", snapshotDir)
        }
        val zipFile = File(snapshotDir, "$formattedFileName.zip")

        Zipper(zipFile).zip(
            listOf(Zipper.Entry(name = "$formattedFileName.txt", path = debugLogger.runningLog))
        )

        return Snapshot(path = zipFile)
    }

    data class Snapshot(val path: File) {
        fun delete() = path.delete()
    }

    companion object {
        private const val TAG = "LogSnapshots"
    }
}
