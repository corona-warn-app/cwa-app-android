package de.rki.coronawarnapp.crash

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class CrashReportEntity(
    var deviceInfo: String,
    var appVersionName: String,
    var appVersionCode: Int,
    var apiLevel: Int,
    var androidVersion: String,
    var shortID: String,
    var message: String,
    var stackTrace: String,
    var tag: String? = null,
    var crashedAt: Date = Date()
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L
}
