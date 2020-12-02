package de.rki.coronawarnapp.util

import android.content.Context
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeStatus @Inject constructor(
    @AppContext private val context: Context,
    @AppScope private val appScope: CoroutineScope
) {

    val isBackgroundRestricted: Flow<Boolean> = flow {
        while (true) {
            try {
                emit(pollIsBackgroundRestricted())
                delay(POLLING_DELAY_MS)
            } catch (e: CancellationException) {
                Timber.d("isBackgroundRestricted was cancelled")
                break
            }
        }
    }
        .distinctUntilChanged()
        .onCompletion {
            if (it != null) Timber.w(it, "isBackgroundRestricted failed.")
        }
        .shareLatest(
            tag = "isBackgroundRestricted",
            scope = appScope
        )

    val isAutoModeEnabled: Flow<Boolean> = flow {
        while (true) {
            try {
                emit(pollIsAutoMode())
                delay(POLLING_DELAY_MS)
            } catch (e: CancellationException) {
                Timber.d("isAutoModeEnabled was cancelled")
                break
            }
        }
    }
        .distinctUntilChanged()
        .onCompletion {
            if (it != null) Timber.w(it, "autoModeEnabled failed.")
        }
        .shareLatest(
            tag = "autoModeEnabled",
            scope = appScope
        )

    private fun pollIsBackgroundRestricted(): Boolean {
        return ConnectivityHelper.isBackgroundRestricted(context)
    }

    private fun pollIsAutoMode(): Boolean {
        return ConnectivityHelper.autoModeEnabled(context)
    }

    companion object {
        private const val POLLING_DELAY_MS = 1000L
    }
}
