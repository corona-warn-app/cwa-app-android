package de.rki.coronawarnapp.ui.coronatest.rat.profile.create

sealed class Navigation {
    object Back : Navigation()
    object ProfileScreen: Navigation()
}

