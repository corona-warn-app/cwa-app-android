package de.rki.coronawarnapp.contactdiary.ui.day

sealed class ContactDiaryDayNavigationEvents {
    object NavigateToOverviewFragment : ContactDiaryDayNavigationEvents()
    object NavigateToAddPersonFragment : ContactDiaryDayNavigationEvents()
    object NavigateToAddLocationFragment : ContactDiaryDayNavigationEvents()
}
