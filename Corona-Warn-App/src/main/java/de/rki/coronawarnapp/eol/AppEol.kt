package de.rki.coronawarnapp.eol

import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
    eolSetting: EolSetting
) {
    val isEol = combine(
        intervalFlow(60_000L),
        eolSetting.eolDateTime
    ) { _, dateTime ->
        ZonedDateTime.now(ZoneId.of("CET")) >= dateTime
    }.distinctUntilChanged()
        .shareLatest(scope = appScope)
}
