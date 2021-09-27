package de.rki.coronawarnapp.rootdetection

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class RootDetectionCheck @Inject constructor(
    @AppContext context: Context,
    private val dispatcherProvider: DispatcherProvider
) {
    private val rootBeer = RootBeer(context)

    // Check should run in a background thread cause it uses I/O
    suspend fun checkRoot() = withContext(dispatcherProvider.IO) {
        Timber.d("checkRoot()")
        try {
            rootBeer.isRooted
        } catch (e: Exception) {
            Timber.e(e, "Root detection failed")
            false
        }.also { Timber.d("Device is rooted: %s", it) }
    }
}
