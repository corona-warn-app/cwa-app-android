package de.rki.coronawarnapp.util.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import de.rki.coronawarnapp.storage.DATABASE_NAME
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.errors.causes
import org.joda.time.Instant
import timber.log.Timber
import java.io.File
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This tool determines the narrow scope for which we will recovery from an encryption error
 * by resetting our encrypted data.
 * This will allow users currently affected by it, that update the app, to keep using it without
 * requiring any manual actions from their side.
 *
 * https://github.com/corona-warn-app/cwa-app-android/issues/642
 */
@Singleton
class EncryptionErrorResetTool @Inject constructor(
    @AppContext private val context: Context
) {
    // https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/app/ContextImpl.java;drc=3b8e8d76315f6718a982d5e6a019b3aa4f634bcd;l=626
    private val encryptedPreferencesFile by lazy {
        val appbaseDir = context.filesDir.parentFile!!
        val sharedPrefsDir = File(appbaseDir, "shared_prefs")
        File(sharedPrefsDir, "${SecurityConstants.ENCRYPTED_SHARED_PREFERENCES_FILE}.xml")
    }
    private val encryptedDatabaseFile by lazy {
        context.getDatabasePath(DATABASE_NAME)
    }
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("encryption_error_reset_tool", Context.MODE_PRIVATE)
    }

    private var isResetWindowConsumed: Boolean
        get() = prefs.getBoolean(PKEY_EA1851_WAS_WINDOW_CONSUMED, false)
        set(value) = prefs.edit {
            putBoolean(PKEY_EA1851_WAS_WINDOW_CONSUMED, value)
        }

    private var resetPerformedAt: Instant?
        get() = prefs.getLong(PKEY_EA1851_RESET_PERFORMED_AT, -1).let {
            if (it == -1L) null else Instant.ofEpochMilli(it)
        }
        set(value) = prefs.edit {
            if (value != null) {
                putLong(PKEY_EA1851_RESET_PERFORMED_AT, value.millis)
            } else {
                remove(PKEY_EA1851_RESET_PERFORMED_AT)
            }
        }

    var isResetNoticeToBeShown: Boolean
        get() = prefs.getBoolean(PKEY_EA1851_SHOW_RESET_NOTICE, false)
        set(value) = prefs.edit {
            putBoolean(PKEY_EA1851_SHOW_RESET_NOTICE, value)
        }

    fun tryResetIfNecessary(error: Throwable): Boolean {
        Timber.w(error, "isRecoveryWarranted()")

        // We only reset for the first error encountered after upgrading to 1.4.0+
        if (isResetWindowConsumed) {
            Timber.v("Reset window has been consumed -> no reset.")
            return false
        }
        isResetWindowConsumed = true

        val keyException = error.causes().singleOrNull { it is GeneralSecurityException }
        if (keyException == null) {
            Timber.v("Error has no GeneralSecurityException as cause -> no reset.")
            return false
        }

        // https://github.com/google/tink/blob/a8ec74d083068cd5e1ebed86fd8254630617b592/java_src/src/main/java/com/google/crypto/tink/aead/AeadWrapper.java#L83
        if (keyException.message?.trim()?.equals("decryption failed") != true) {
            Timber.v("Not the GeneralSecurityException we are looking for -> no reset.")
            return false
        }

        if (!encryptedPreferencesFile.exists()) {
            // The error we are looking for can only happen if there already is an encrypted preferences file
            Timber.w(
                "Error fits, but where is the existing preference file (%s)? -> no reset.",
                encryptedPreferencesFile
            )
            return false
        }

        // So we have a GeneralSecurityException("decryption failed") and existing preferences
        // And this is the first error we encountered after upgrading to 1.4.0, let's do this!

        return try {
            performReset()
        } catch (e: Exception) {
            // If anything goes wrong, we return false, so that our caller can rethrow the original error.
            Timber.e(e, "Error while performing the reset.")
            false
        }
    }

    private fun performReset(): Boolean {
        // Delete encrypted shared preferences file
        if (!encryptedPreferencesFile.delete()) {
            Timber.w("Couldn't delete %s", encryptedPreferencesFile)
            // The encrypted preferences are a prerequisite for this error case
            // If we can't delete that, we have to assume our reset approach failed.
            return false
        }

        // The user may have encountered the error even before a database was created.
        if (encryptedDatabaseFile.exists() && !encryptedDatabaseFile.delete()) {
            Timber.w("Couldn't delete %s", encryptedDatabaseFile)
            // There was a database, but we couldn't delete it
            // The database is inaccessible without the matching preferences (which we just deleted).
            return false
        }

        resetPerformedAt = Instant.now()
        isResetNoticeToBeShown = true

        return true
    }

    companion object {
        private const val PKEY_EA1851_RESET_PERFORMED_AT = "ea1851.reset.performedAt"
        private const val PKEY_EA1851_WAS_WINDOW_CONSUMED = "ea1851.reset.windowconsumed"
        private const val PKEY_EA1851_SHOW_RESET_NOTICE = "ea1851.reset.shownotice"
    }
}
