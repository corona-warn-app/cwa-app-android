package de.rki.coronawarnapp.contactdiary.util

import android.content.Context
import de.rki.coronawarnapp.R
import io.mockk.every
import io.mockk.slot

fun mockStringsForContactDiaryExporterTests(context: Context) {

    val fromSlot = slot<String>()
    val toSlot = slot<String>()

    every {
        context.getString(
            R.string.contact_diary_export_intro_one,
            capture(fromSlot),
            capture(toSlot)
        )
    } answers { "Kontakte der letzten 15 Tage (${fromSlot.captured} - ${toSlot.captured})" }

    every {
        context.getString(R.string.contact_diary_export_intro_two)
    } answers {
        "Die nachfolgende Liste dient dem zuständigen Gesundheitsamt zur Kontaktnachverfolgung gem. § 25 IfSG."
    }

    every { context.getString(R.string.contact_diary_export_prefix_phone) } returns "Tel."
    every { context.getString(R.string.contact_diary_export_prefix_email) } returns "eMail"

    every { context.getString(R.string.contact_diary_export_durations_less_than_10min) } returns
        "Kontaktdauer < 10 Minuten"
    every { context.getString(R.string.contact_diary_export_durations_longer_than_10min) } returns
        "Kontaktdauer > 10 Minuten"

    every { context.getString(R.string.contact_diary_export_wearing_mask) } returns "mit Maske"
    every { context.getString(R.string.contact_diary_export_wearing_no_mask) } returns "ohne Maske"

    every { context.getString(R.string.contact_diary_export_outdoor) } returns "im Freien"
    every { context.getString(R.string.contact_diary_export_indoor) } returns "im Gebäude"

    every { context.getString(R.string.contact_diary_export_location_duration_prefix) } returns "Dauer"
    every { context.getString(R.string.contact_diary_export_location_duration_suffix) } returns "h"

    every { context.getString(R.string.contact_diary_corona_test_pcr_title) } returns "PCR-Test registriert"
    every { context.getString(R.string.contact_diary_corona_test_rat_title) } returns "Schnelltest durchgeführt"
    every { context.getString(R.string.contact_diary_corona_test_positive) } returns "Befund positiv"
    every { context.getString(R.string.contact_diary_corona_test_negative) } returns "Befund negativ"
}
