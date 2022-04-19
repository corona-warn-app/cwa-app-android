package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

import de.rki.coronawarnapp.profile.model.ProfileId

sealed class CreateRATProfileNavigation {
    object Back : CreateRATProfileNavigation()
    data class ProfileScreen(val profileId: ProfileId) : CreateRATProfileNavigation()
}
