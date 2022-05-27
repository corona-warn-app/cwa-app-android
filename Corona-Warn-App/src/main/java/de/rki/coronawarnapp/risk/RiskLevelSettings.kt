package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskLevelSettings @Inject constructor(
    @AppContext private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences(NAME_SHARED_PREFS, Context.MODE_PRIVATE)
    }

    /**
     * The identifier of the config used during the last risklevel calculation
     */
    var lastUsedConfigIdentifier: String?
        get() = prefs.getString(PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID, null)
        set(value) = prefs.edit(true) {
            putString(PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID, value)
        }

    var ewLastChangeCheckedRiskLevelTimestamp: Instant?
        get() = prefs.getLong(PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW, 0L).let {
            if (it != 0L) Instant.ofEpochMilli(it) else null
        }
        set(value) = prefs.edit {
            putLong(PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW, value?.toEpochMilli() ?: 0L)
        }

    var lastChangeCheckedRiskLevelCombinedTimestamp: Instant?
        get() = prefs.getLong(PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED, 0L).let {
            if (it != 0L) Instant.ofEpochMilli(it) else null
        }
        set(value) = prefs.edit {
            putLong(PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED, value?.toEpochMilli() ?: 0L)
        }

    companion object {
        private const val NAME_SHARED_PREFS = "risklevel_localdata"
        private const val PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID = "risklevel.config.identifier.last"

        /*
        * Change was last checked at
        * */
        private const val PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_EW =
            "PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID" // seems to be a copy/paste mistake that lives on...
        private const val PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED =
            "PKEY_LAST_CHANGE_CHECKED_RISKLEVEL_TIMESTAMP_COMBINED"
    }
}
