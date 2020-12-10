package de.rki.coronawarnapp.update

import android.content.Intent
import android.net.Uri
import dagger.Reusable
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import timber.log.Timber
import javax.inject.Inject

@Reusable
class UpdateChecker @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    suspend fun checkForUpdate(): Result = try {
        val isUpdateNeeded = checkIfUpdatesNeededFromServer()
        Result(isUpdateNeeded = isUpdateNeeded, updateIntent = createUpdateIntent())
    } catch (exception: ApplicationConfigurationCorruptException) {
        Timber.e(
            "ApplicationConfigurationCorruptException caught:%s",
            exception.localizedMessage
        )

        Result(isUpdateNeeded = true, updateIntent = createUpdateIntent())
    } catch (exception: Exception) {
        Timber.tag(TAG).e("Exception caught:%s", exception.localizedMessage)
        Result(isUpdateNeeded = false, updateIntent = null)
    }

    private suspend fun checkIfUpdatesNeededFromServer(): Boolean {
        val cwaAppConfig: CWAConfig = appConfigProvider.getAppConfig()

        val minVersionFromServer = cwaAppConfig.minVersionCode

        Timber.tag(TAG).d("minVersionFromServer:%s", minVersionFromServer)
        Timber.tag(TAG).d("Current app version:%s", BuildConfig.VERSION_CODE)

        val needsImmediateUpdate = VersionComparator.isVersionOlder(
            BuildConfig.VERSION_CODE.toLong(),
            minVersionFromServer
        )
        Timber.tag(TAG).e("needs update:$needsImmediateUpdate")
        return needsImmediateUpdate
    }

    private fun createUpdateIntent(): Intent {
        val uriStringInPlayStore = STORE_PREFIX + BuildConfig.APPLICATION_ID
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                uriStringInPlayStore
            )
            setPackage(COM_ANDROID_VENDING)
        }
    }

    data class Result(
        val isUpdateNeeded: Boolean,
        val updateIntent: Intent?
    )

    companion object {
        private const val TAG: String = "UpdateChecker"

        private const val STORE_PREFIX = "https://play.google.com/store/apps/details?id="
        private const val COM_ANDROID_VENDING = "com.android.vending"
    }
}
