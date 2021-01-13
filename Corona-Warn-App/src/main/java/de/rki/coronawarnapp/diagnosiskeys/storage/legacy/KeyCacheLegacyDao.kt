package de.rki.coronawarnapp.diagnosiskeys.storage.legacy

import androidx.room.Dao
import androidx.room.Query

@Dao
interface KeyCacheLegacyDao {
    @Query("DELETE FROM date")
    suspend fun clear()
}
