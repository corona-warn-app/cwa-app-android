package de.rki.coronawarnapp.eol

import androidx.work.WorkManager
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks end of life date
 */

@Singleton
class AppEol @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val workManager: WorkManager,
    eolSetting: EolSetting
) {
    val isEol = combine(
        intervalFlow(60_000L),
        eolSetting.eolDateTime
    ) { _, dateTime ->
        ZonedDateTime.now(ZoneId.of("CET")) >= dateTime
    }.distinctUntilChanged()
        .onEach { isEol ->
            if (isEol) {
                Timber.tag(TAG).d("Cancel all works ")
                workManager.cancelAllWork()
            }
        }
        .shareLatest(scope = appScope)

    companion object {
        private val TAG = tag<AppEol>()
    }
}
