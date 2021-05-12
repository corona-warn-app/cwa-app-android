package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

sealed class CreateRATProfileNavigation {
    object Back : CreateRATProfileNavigation()
    object ProfileScreen : CreateRATProfileNavigation()
}
