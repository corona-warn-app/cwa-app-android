package de.rki.coronawarnapp.update

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.environment.BuildConfigWrap
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
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
        val needsImmediateUpdate = VersionComparator.isVersionOlder(currentVersion, minVersionFromServer)
        Timber.tag(TAG).d("Needs update:$needsImmediateUpdate")
        return if (needsImmediateUpdate) assertUpdateIsNeeded() else needsImmediateUpdate
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun assertUpdateIsNeeded(): Boolean {
        val cwaAppConfig: CWAConfig = try {
            withTimeout(UPDATE_CHECK_TIMEOUT) { appConfigProvider.getAppConfig() }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "assertUpdateIsNeeded failed, rolling back to cached config")
            appConfigProvider.currentConfig.first()
        }
        val updateStillNeeded = VersionComparator.isVersionOlder(
            BuildConfigWrap.VERSION_CODE,
            cwaAppConfig.minVersionCode
        )

        Timber.tag(TAG).d("assertUpdateIsNeeded updateStillNeeded:$updateStillNeeded")
        return updateStillNeeded
    }

    data class Result(val isUpdateNeeded: Boolean)
    companion object {
        private const val UPDATE_CHECK_TIMEOUT = 5_000L
        private const val TAG = "UpdateChecker"
    }
}
