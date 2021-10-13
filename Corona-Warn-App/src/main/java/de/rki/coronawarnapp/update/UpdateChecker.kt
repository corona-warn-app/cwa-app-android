package de.rki.coronawarnapp.update

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.environment.BuildConfigWrap
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

@Reusable
class UpdateChecker @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    suspend fun checkForUpdate(): Result = try {
        if (isUpdateNeeded()) {
            Result(isUpdateNeeded = true)
        } else {
            Result(isUpdateNeeded = false)
        }
    } catch (exception: ApplicationConfigurationCorruptException) {
        Timber.e(
            "ApplicationConfigurationCorruptException caught:%s",
            exception.localizedMessage
        )

        Result(isUpdateNeeded = true)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Update check failed, network connection?")
        Result(isUpdateNeeded = false)
    }

    private suspend fun isUpdateNeeded(): Boolean {
        val cwaAppConfig: CWAConfig = appConfigProvider.currentConfig.first()

        val minVersionFromServer = cwaAppConfig.minVersionCode

        val currentVersion = BuildConfigWrap.VERSION_CODE

        Timber.tag(TAG).d("Config minVersionCode:%s", minVersionFromServer)
        Timber.tag(TAG).d("App versionCode:%s", currentVersion)
        val needsImmediateUpdate = VersionComparator.isVersionOlder(
            currentVersion,
            minVersionFromServer
        )
        Timber.tag(TAG).d("Needs update:$needsImmediateUpdate")
        return needsImmediateUpdate
    }

    data class Result(val isUpdateNeeded: Boolean)
    companion object {

        private const val TAG = "UpdateChecker"
    }
}
