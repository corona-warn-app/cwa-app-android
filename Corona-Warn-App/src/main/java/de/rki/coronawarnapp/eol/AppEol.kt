package de.rki.coronawarnapp.eol

import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.flow.shareLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks end of life date
 */
fun isEol(
    now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("CET"))
): Boolean = now >= ZonedDateTime.parse("2023-06-01T00:00:00+02:00")

inline fun ifEol(action: () -> Unit) {
    if (isEol()) action()
}

inline fun ifNotEol(action: () -> Unit) {
    if (!isEol()) action()
}

@Singleton
class AppEol @Inject constructor(
    @AppScope private val appScope: CoroutineScope
) {
    val isEol: Flow<Boolean> = intervalFlow(10_000L)
        .map { isEol() }
        .distinctUntilChanged()
        .shareLatest(scope = appScope)
}


