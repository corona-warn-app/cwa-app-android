package de.rki.coronawarnapp.nearby

import android.content.Context
import androidx.core.content.edit
import org.joda.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ENFClientLocalData @Inject constructor(
    private val context: Context
) {

    private val prefs by lazy {
        context.getSharedPreferences("enfclient_localdata", Context.MODE_PRIVATE)
    }

    var lastQuotaResetAt: Instant
        get() = Instant.ofEpochMilli(prefs.getLong(PKEY_QUOTA_LAST_RESET, 0L))
        set(value) = prefs.edit(true) {
            putLong(PKEY_QUOTA_LAST_RESET, value.millis)
        }

    var currentQuota: Int
        get() = prefs.getInt(PKEY_QUOTA_CURRENT, 0)
        set(value) = prefs.edit(true) {
            putInt(PKEY_QUOTA_CURRENT, value)
        }

    companion object {
        private const val PKEY_QUOTA_LAST_RESET = "enfclient.quota.lastreset"
        private const val PKEY_QUOTA_CURRENT = "enfclient.quota.current"
    }
}
