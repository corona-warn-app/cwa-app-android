package de.rki.coronawarnapp.crash

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CrashReportDao {

    @Insert
    suspend fun insertCrashReport(crashReportEntity: CrashReportEntity)

    @Query("DELETE FROM CrashReportEntity")
    suspend fun deleteAllCrashReports()

    @Query("SELECT * FROM CrashReportEntity")
    fun getAllCrashReports(): LiveData<List<CrashReportEntity>>
}
