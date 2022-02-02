package de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database

import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.util.serialization.SerializationModule
import timber.log.Timber

class DccWalletInfoConverter {
    private val objectMapper = SerializationModule().jacksonObjectMapper()

    @TypeConverter
    fun toDccWalletInfo(value: String): DccWalletInfo? = try {
        objectMapper.readValue(value)
    } catch (e: Exception) {
        Timber.e(e, "Can't create DccWalletInfo")
        null
    }

    @TypeConverter
    fun fromDccWalletInfo(dccWalletInfo: DccWalletInfo): String {
        return objectMapper.writeValueAsString(dccWalletInfo)
    }
}
