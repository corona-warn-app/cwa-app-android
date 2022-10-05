package de.rki.coronawarnapp.contactdiary.ui.overview

import java.time.LocalDate

sealed class ContactDiaryOverviewNavigationEvents {
    object NavigateToMainActivity : ContactDiaryOverviewNavigationEvents()
    class NavigateToContactDiaryDayFragment(localDate: LocalDate) : ContactDiaryOverviewNavigationEvents() {
        val localDateString = localDate.toString()
    }
}
