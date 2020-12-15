package de.rki.coronawarnapp.contactdiary.ui.day

sealed class ContactDiaryDayNavigationEvents {
    object NavigateToOverviewFragment : ContactDiaryDayNavigationEvents()
    object NavigateToAddPersonBottomSheet : ContactDiaryDayNavigationEvents()
    object NavigateToAddLocationBottomSheet : ContactDiaryDayNavigationEvents()
}
