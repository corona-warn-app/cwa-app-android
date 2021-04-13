package de.rki.coronawarnapp.presencetracing.checkins.checkout

import androidx.annotation.VisibleForTesting
import dagger.Reusable
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.split.splitByMidnightUTC
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUtc
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import kotlinx.coroutines.flow.first
import org.joda.time.Duration
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToLong

@Reusable
class ContactJournalCheckInEntryCreator @Inject constructor(
    private val diaryRepository: ContactDiaryRepository
) {

    suspend fun createEntry(checkIn: CheckIn) {
        Timber.d("Creating journal entry for %s", checkIn)

        // 1. Create location if missing
        val location = checkIn.createLocationIfMissing()

        // 2. Split CheckIn by Midnight UTC
        val splitCheckIns = checkIn.splitByMidnightUTC()
        Timber.d("Split %s into %s ", this, splitCheckIns)

        // 3. Create LocationVisit if missing
        splitCheckIns
            .createMissingLocationVisits(location)
            .forEach { diaryRepository.addLocationVisit(it) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun CheckIn.createLocationIfMissing(): ContactDiaryLocation = diaryRepository.locations.first()
        .find { it.traceLocationID == traceLocationId } ?: createLocationEntry()

    private suspend fun CheckIn.createLocationEntry(): ContactDiaryLocation {
        Timber.d("Creating new contact diary location from %s", this)
        val location = DefaultContactDiaryLocation(
            locationName = toLocationName(),
            traceLocationID = traceLocationId
        )

        // Get location from db cause we need the id autogenerated by db
        return diaryRepository.addLocation(location)
            .also { Timber.d("Created %s and added it to contact journal db", it) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun CheckIn.toLocationName(): String {
        val nameParts = mutableListOf(description, address)

        if (traceLocationStart != null && traceLocationEnd != null) {
            if (traceLocationStart.millis > 0 && traceLocationEnd.millis > 0) {
                val formattedStartDate = traceLocationStart.toUserTimeZone().toString(DateTimeFormat.shortDateTime())
                val formattedEndDate = traceLocationEnd.toUserTimeZone().toString(DateTimeFormat.shortDateTime())
                nameParts.add("$formattedStartDate - $formattedEndDate")
            }
        }

        return nameParts.joinToString(separator = ", ")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun CheckIn.toLocationVisit(location: ContactDiaryLocation): ContactDiaryLocationVisit {
        // Duration column is set by calculating the time difference in minutes between Check-in StartDate
        // and Check-in EndDate and rounding it to the closest 15-minute duration
        // Use Seconds for more precision
        val durationInMinutes = Seconds.secondsBetween(checkInStart, checkInEnd).seconds / 60.0
        val duration = (durationInMinutes / 15).roundToLong() * 15
        return DefaultContactDiaryLocationVisit(
            date = checkInStart.toLocalDateUtc(),
            contactDiaryLocation = location,
            duration = Duration.standardMinutes(duration),
            checkInID = id
        ).also { Timber.d("Created %s for %s", it, this) }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun List<CheckIn>.createMissingLocationVisits(location: ContactDiaryLocation):
        List<ContactDiaryLocationVisit> {
            Timber.d(
                "createMissingLocationVisits(location=%s) for %s",
                location,
                this.joinToString(prefix = System.lineSeparator(), separator = System.lineSeparator())
            )
            val existingLocationVisits = diaryRepository.locationVisits.first()
            // Existing location visits shall not be updated, so just drop them
            return filter {
                existingLocationVisits.none { visit ->
                    visit.date == it.checkInStart.toLocalDateUtc() &&
                        visit.contactDiaryLocation.locationId == location.locationId
                }
            }
                .map { it.toLocationVisit(location) }
                .also {
                    Timber.d(
                        "Created location visits: %s",
                        it.joinToString(prefix = System.lineSeparator(), separator = System.lineSeparator())
                    )
                }
        }
}
