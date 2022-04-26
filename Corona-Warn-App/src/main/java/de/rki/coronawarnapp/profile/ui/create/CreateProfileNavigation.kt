package de.rki.coronawarnapp.profile.ui.create

import de.rki.coronawarnapp.profile.model.ProfileId

sealed class CreateProfileNavigation {
    object Back : CreateProfileNavigation()
    data class ProfileScreen(val profileId: ProfileId) : CreateProfileNavigation()
}
