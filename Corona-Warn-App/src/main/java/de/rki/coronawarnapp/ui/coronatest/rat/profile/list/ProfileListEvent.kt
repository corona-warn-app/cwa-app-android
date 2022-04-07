package de.rki.coronawarnapp.ui.coronatest.rat.profile.list

sealed class ProfileListEvent {
    object NavigateToAddProfile: ProfileListEvent()
}
