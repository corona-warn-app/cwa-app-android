package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.R

enum class PersonOverviewItemColor(
    @ColorRes starsTint: Int,
    @DrawableRes background: Int
) {
    COLOR_1(R.color.starsColor1, R.drawable.bg_person_overview_1),
    COLOR_2(R.color.starsColor2, R.drawable.bg_person_overview_2),
    COLOR_3(R.color.starsColor3, R.drawable.bg_person_overview_3),
}
