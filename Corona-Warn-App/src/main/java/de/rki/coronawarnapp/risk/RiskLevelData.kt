package de.rki.coronawarnapp.risk

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiskLevelData @Inject constructor(
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

    companion object {
        private const val NAME_SHARED_PREFS = "risklevel_localdata"
        private const val PKEY_RISKLEVEL_CALC_LAST_CONFIG_ID = "risklevel.config.identifier.last"
    }
}
