package de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject

@Database(
    entities = [DccWalletInfoEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DccWalletInfoConverter::class)
abstract class DccWalletInfoDatabase : RoomDatabase() {

    abstract fun dccWalletInfoDao(): DccWalletInfoDao

    class Factory @Inject constructor(@AppContext private val context: Context) {
        fun create(): DccWalletInfoDatabase = Room
            .databaseBuilder(context, DccWalletInfoDatabase::class.java, DCC_WALLET_INFO_DATABASE_NAME)
            .build()
    }

    companion object {
        private const val DCC_WALLET_INFO_DATABASE_NAME = "DCCWalletInfo-db"
    }
}
