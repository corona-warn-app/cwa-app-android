package de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo

@Entity(tableName = "person_wallet_info")
data class DccWalletInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "person_identifier")
    val groupKey: String,

    @ColumnInfo(name = "dcc_wallet_info")
    val dccWalletInfo: DccWalletInfo
)
