package de.rki.coronawarnapp.profile.ui.list

sealed class ProfileListEvent {
    object NavigateToAddProfile : ProfileListEvent()
    data class OpenProfile(val id: Int, val position: Int) : ProfileListEvent()
}
