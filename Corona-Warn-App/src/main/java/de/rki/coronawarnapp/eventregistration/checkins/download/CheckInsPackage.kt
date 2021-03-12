package de.rki.coronawarnapp.eventregistration.checkins.download

import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import org.joda.time.DateTime
import org.joda.time.Instant

interface CheckInsPackage {

    /**
     * Hides the file reading
     */
    suspend fun extractCheckIns(): List<CheckIn>
}

// proprietary dummy implementation
object DummyCheckInPackage : CheckInsPackage {
    override suspend fun extractCheckIns(): List<CheckIn> {
        return listOf(dummyEventCheckIn1)
    }
}

private const val TYPE_LOCATION = 1
private const val TYPE_EVENT = 2

private val dummyEventCheckIn1: CheckIn = CheckIn(
    id = 1L,
    guid = "eventOne",
    version = 1,
    type = TYPE_LOCATION,
    description = "Pizza place",
    address = "Around the corner",
    traceLocationStart = null,
    traceLocationEnd = null,
    defaultCheckInLengthInMinutes = null,
    signature = "signature",
    checkInStart = Instant.ofEpochMilli(
        DateTime(2021, 2, 20, 11, 45).millis
    ),
    checkInEnd = Instant.ofEpochMilli(
        DateTime(2021, 2, 20, 12, 15).millis
    ),
    targetCheckInEnd = null,
    createJournalEntry = false
)

