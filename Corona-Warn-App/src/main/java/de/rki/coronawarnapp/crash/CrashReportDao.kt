package de.rki.coronawarnapp.crash

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import javax.inject.Singleton

@Singleton
@Dao
interface CrashReportDao {

    @Insert
    suspend fun insertCrashReport(crashReportEntity: CrashReportEntity)

    @Query("DELETE FROM CrashReportEntity")
    suspend fun deleteAllCrashReports()

    @Query("SELECT * FROM CrashReportEntity")
    fun getAllCrashReportsLiveData(): LiveData<List<CrashReportEntity>>

    @Query("SELECT * FROM CrashReportEntity WHERE id = :id")
    fun getCrashReportForIdLiveData(id: Long): LiveData<CrashReportEntity>
}
