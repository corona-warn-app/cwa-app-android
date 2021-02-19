package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import org.joda.time.LocalDate

object ContactDiaryData {

    val TWO_PERSONS_NO_ADDITIONAL_DATA = listOf(
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Andrea Steinhauer"
            )
        ),
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Constantin Frenzel"
            )
        )
    )

    val TWO_PERSONS_WITH_PHONE_NUMBERS = listOf(
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Andrea Steinhauer",
                phoneNumber = "+49 123 456789"
            )
        ),
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Constantin Frenzel",
                phoneNumber = "+49 987 654321"
            )
        )
    )

    val TWO_PERSONS_WITH_PHONE_NUMBERS_AND_EMAIL = listOf(
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Andrea Steinhauer",
                phoneNumber = "+49 123 456789",
                emailAddress = "andrea.steinhauer@example.com"
            )
        ),
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Constantin Frenzel",
                phoneNumber = "+49 987 654321",
                emailAddress = "constantin.frenzel@example.com"
            )
        )
    )

    val TWO_PERSONS_WITH_ATTRIBUTES = listOf(
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Andrea Steinhauer"
            ),
            withMask = true,
            wasOutside = true,
            durationClassification = ContactDiaryPersonEncounter.DurationClassification.LESS_THAN_15_MINUTES

        ),
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Constantin Frenzel"
            ),
            withMask = false,
            wasOutside = false,
            durationClassification = ContactDiaryPersonEncounter.DurationClassification.MORE_THAN_15_MINUTES
        )
    )

    val TWO_PERSONS_WITH_CIRCUMSTANCES = listOf(
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Andrea Steinhauer"
            ),
            circumstances = "Sicherheitsmaßnahmen eingehalten"
        ),
        DefaultContactDiaryPersonEncounter(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryPerson = DefaultContactDiaryPerson(
                fullName = "Constantin Frenzel"
            ),
            circumstances = "saßen nah beieinander"
        )
    )

    val TWO_LOCATIONS_NO_ADDITIONAL_DATA = listOf(
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Bakery"
            )
        ),
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Barber"
            )
        )
    )

    val TWO_LOCATIONS_WITH_EMAIL = listOf(
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Bakery",
                emailAddress = "baker@ibakeyourbread.com"
            )
        ),
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Barber",
                emailAddress = "barber@icutyourhair.com"
            )
        )
    )

    val TWO_LOCATIONS_WITH_PHONE_NUMBERS_AND_EMAIL = listOf(
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Bakery",
                phoneNumber = "+11 222 333333",
                emailAddress = "baker@ibakeyourbread.com"
            )
        ),
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Barber",
                phoneNumber = "+99 888 777777",
                emailAddress = "barber@icutyourhair.com"
            )
        )
    )

    val TWO_LOCATIONS_WITH_DURATION = listOf(
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Bakery"
            ),
            duration = 15L
        ),
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Barber"
            ),
            duration = 105L
        )
    )

    val TWO_LOCATIONS_WITH_CIRCUMSTANCES = listOf(
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-01"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Bakery"
            ),
            circumstances = "Very crowdy, but delicious bread"
        ),
        DefaultContactDiaryLocationVisit(
            date = LocalDate.parse("2021-01-02"),
            contactDiaryLocation = DefaultContactDiaryLocation(
                locationName = "Barber"
            ),
            circumstances = "Nobody was wearing a mask, but needed a haircut real bad"
        )
    )
}
