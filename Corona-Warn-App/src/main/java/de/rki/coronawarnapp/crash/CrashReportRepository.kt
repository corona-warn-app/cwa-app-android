package de.rki.coronawarnapp.crash

import android.os.Build
import android.util.Log
import de.rki.coronawarnapp.BuildConfig

class CrashReportRepository(private val crashReportDao: CrashReportDao) {

    val allCrashReports = crashReportDao.getAllCrashReports()

    suspend fun createCrashReport(message: String, t: Throwable?, tag: String?) {
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE}"
        val appVersionName = BuildConfig.VERSION_NAME
        val appVersionCode = BuildConfig.VERSION_CODE
        val apiLevel = Build.VERSION.SDK_INT
        val androidVersion = Build.VERSION.RELEASE
        // TODO: Save git commit it via gradle
        val shortID = "fe9128f"
        val stackTrace = Log.getStackTraceString(t)

        insertCrashReport(
            CrashReportEntity(
                deviceInfo,
                appVersionName,
                appVersionCode,
                apiLevel,
                androidVersion,
                shortID,
                message,
                stackTrace,
                tag
            )
        )
    }

    suspend fun insertCrashReport(crashReportEntity: CrashReportEntity) {
        crashReportDao.insertCrashReport(crashReportEntity)
    }

    suspend fun deleteAllCrashReports() {
        crashReportDao.deleteAllCrashReports()
    }
}
