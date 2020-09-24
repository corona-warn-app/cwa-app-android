package de.rki.coronawarnapp.util.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import de.rki.coronawarnapp.util.RetryMechanism
import timber.log.Timber
import java.security.KeyException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedPreferencesFactory @Inject constructor(
    private val context: Context,
) {

    private val masterKeyAlias by lazy {
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    private fun createInstance(fileName: String) = EncryptedSharedPreferences.create(
        fileName,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun create(fileName: String): SharedPreferences = try {
        RetryMechanism.retryWithBackOff {
            createInstance(fileName).also {
                Timber.d("Instance created, %d entries.", it.all.size)
            }
        }
    } catch (e: Exception) {
        throw KeyException("Permantly failed to instantiate encrypted preferences", e)
    }
}
