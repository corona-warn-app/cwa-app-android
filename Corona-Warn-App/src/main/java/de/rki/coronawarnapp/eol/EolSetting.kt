package de.rki.coronawarnapp.eol

import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EolSetting @Inject constructor() {
    val eolDateTime = flowOf(ZonedDateTime.parse("2023-06-01T00:00:00+02:00"))
}
