package de.rki.coronawarnapp.util.encryptionmigration

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.util.di.AppContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionErrorResetTool @Inject constructor(
    @AppContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("encryption_error_reset_tool", Context.MODE_PRIVATE)
    }

    var isResetNoticeToBeShown: Boolean
        get() = prefs.getBoolean(PKEY_EA2850_SHOW_RESET_NOTICE, false)
        set(value) = prefs.edit {
            putBoolean(PKEY_EA2850_SHOW_RESET_NOTICE, value)
        }

    companion object {
        private const val PKEY_EA2850_SHOW_RESET_NOTICE = "ea2850.reset.shownotice"
    }
}
