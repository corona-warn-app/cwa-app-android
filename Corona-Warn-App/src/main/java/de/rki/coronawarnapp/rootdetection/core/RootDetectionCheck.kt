package de.rki.coronawarnapp.rootdetection.core

import com.scottyab.rootbeer.RootBeer
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class RootDetectionCheck @Inject constructor(
    private val rootBeer: RootBeer,
    private val dispatcherProvider: DispatcherProvider,
    private val cwaSettings: CWASettings
) {

    suspend fun shouldShowRootInfo(): Boolean = (shouldShow() && isRooted())
        .also { Timber.tag(TAG).d("Should show root info - %b", it) }

    suspend fun suppressRootInfoForCurrentVersion(suppress: Boolean) {
        Timber.tag(TAG).d("suppressRootInfoForCurrentVersion(suppress=%s)", suppress)
        when (suppress) {
            true -> cwaSettings.updateLastSuppressRootInfoVersionCode(currentVersionCode)
            false -> cwaSettings.updateLastSuppressRootInfoVersionCode(DEFAULT_SUPPRESS_ROOT_INFO_FOR_VERSION_CODE)
        }
    }

    // Check should run in a background thread cause it uses I/O
    suspend fun isRooted() = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).d("isRooted()")
        isRooted
            .also { Timber.tag(TAG).d("Device is rooted: %s", it) }
    }

    private val isRooted: Boolean
        get() = try {
            rootBeer.isRooted
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Root detection failed. Returning false")
            false
        }

    private val currentVersionCode
        get() = BuildConfigWrap.VERSION_CODE

    private suspend fun shouldShow(): Boolean {
        val suppressVersionCode = cwaSettings.lastSuppressRootInfoVersionCode.first()
        val showRootInfo = currentVersionCode > suppressVersionCode
        Timber.tag(TAG).d(
            "current version %d is greater than last suppress root info version code %d - %s",
            currentVersionCode,
            suppressVersionCode,
            showRootInfo
        )
        return showRootInfo
    }

    companion object {
        private val TAG = tag<RootDetectionCheck>()

        private const val DEFAULT_SUPPRESS_ROOT_INFO_FOR_VERSION_CODE = 0L
    }
}
