package de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DccWalletInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dccWallet: DccWalletInfoEntity)

    @Delete
    suspend fun delete(dccWallet: DccWalletInfoEntity)

    @Query("SELECT * FROM person_wallet_info")
    fun getAll(): Flow<List<DccWalletInfoEntity>>
}
