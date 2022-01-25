package de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.util.serialization.SerializationModule

class DccWalletInfoConverter {
    private val objectMapper = SerializationModule().jacksonObjectMapper()

    @TypeConverter
    fun toDccWalletInfo(value: String): DccWalletInfo {
        return objectMapper.readValue(value)
    }

    @TypeConverter
    fun fromDccWalletInfo(dccWalletInfo: DccWalletInfo): String {
        return objectMapper.writeValueAsString(dccWalletInfo)
    }
}
