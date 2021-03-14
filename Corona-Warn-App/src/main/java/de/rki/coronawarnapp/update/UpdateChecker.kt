package de.rki.coronawarnapp.update

import android.content.Intent
import android.net.Uri
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.environment.BuildConfigWrap
import timber.log.Timber
import javax.inject.Inject

@Reusable
class UpdateChecker @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    suspend fun checkForUpdate(): Result = try {
        if (isUpdateNeeded()) {
            Result(isUpdateNeeded = true, updateIntent = createUpdateAction())
        } else {
            Result(isUpdateNeeded = false)
        }
    } catch (exception: ApplicationConfigurationCorruptException) {
        Timber.e(
            "ApplicationConfigurationCorruptException caught:%s",
            exception.localizedMessage
        )

        Result(isUpdateNeeded = true, updateIntent = createUpdateAction())
    } catch (exception: Exception) {
        Timber.tag(TAG).e("Exception caught:%s", exception.localizedMessage)
        Result(isUpdateNeeded = false)
    }

    private suspend fun isUpdateNeeded(): Boolean {
        val cwaAppConfig: CWAConfig = appConfigProvider.getAppConfig()

        val minVersionFromServer = cwaAppConfig.minVersionCode

        val currentVersion = BuildConfigWrap.VERSION_CODE

        Timber.tag(TAG).d("minVersionFromServer:%s", minVersionFromServer)
        Timber.tag(TAG).d("Current app version:%s", currentVersion)

        val needsImmediateUpdate = VersionComparator.isVersionOlder(
            currentVersion,
            minVersionFromServer
        )
        Timber.tag(TAG).e("needs update:$needsImmediateUpdate")
        return needsImmediateUpdate
    }

    private fun createUpdateAction(): () -> Intent = {
        val uriStringInPlayStore = STORE_PREFIX + BuildConfig.APPLICATION_ID
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uriStringInPlayStore)
            setPackage(COM_ANDROID_VENDING)
        }
    }

    data class Result(
        val isUpdateNeeded: Boolean,
        val updateIntent: (() -> Intent)? = null
    )

    companion object {
        private const val TAG: String = "UpdateChecker"

        private const val STORE_PREFIX = "https://play.google.com/store/apps/details?id="
        private const val COM_ANDROID_VENDING = "com.android.vending"
    }
}
