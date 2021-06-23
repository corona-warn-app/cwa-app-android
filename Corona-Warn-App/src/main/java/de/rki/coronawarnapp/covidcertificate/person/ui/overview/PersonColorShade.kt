package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.R

enum class PersonColorShade(
    @ColorRes val starsTint: Int,
    @DrawableRes val background: Int,
    @DrawableRes val currentCertificateBg: Int,
) {

    COLOR_1(
        R.color.starsColor1,
        R.drawable.bg_person_overview_1,
        R.drawable.bg_certificate_blue_1
    ),
    COLOR_2(
        R.color.starsColor2,
        R.drawable.bg_person_overview_2,
        R.drawable.bg_certificate_blue_2
    ),
    COLOR_3(
        R.color.starsColor3,
        R.drawable.bg_person_overview_3,
        R.drawable.bg_certificate_blue_3
    );

    @DrawableRes val defaultCertificateBg: Int = R.drawable.bg_certificate_grey

    companion object {
        fun shadeFor(index: Int): PersonColorShade {
            val values = values()
            return values.getOrElse(index.rem(values.size)) { COLOR_1 }
        }
    }
}
