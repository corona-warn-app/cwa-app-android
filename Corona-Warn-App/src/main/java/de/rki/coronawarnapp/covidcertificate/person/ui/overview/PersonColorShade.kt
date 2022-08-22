package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.R

enum class PersonColorShade(
    @ColorRes val starsTint: Int,
    @DrawableRes val background: Int,
    @DrawableRes val currentCertificateBg: Int,
    @DrawableRes val bookmarkIcon: Int,
    @DrawableRes val admissionBadgeBg: Int,
) {

    COLOR_1(
        R.color.starsColor1,
        R.drawable.bg_person_overview_1,
        R.drawable.bg_certificate_blue_1,
        R.drawable.ic_bookmark_blue_1,
        R.drawable.ic_admission_badge_1,
    ),
    COLOR_2(
        R.color.starsColor2,
        R.drawable.bg_person_overview_2,
        R.drawable.bg_certificate_blue_2,
        R.drawable.ic_bookmark_blue_2,
        R.drawable.ic_admission_badge_2,
    ),
    COLOR_3(
        R.color.starsColor3,
        R.drawable.bg_person_overview_3,
        R.drawable.bg_certificate_blue_3,
        R.drawable.ic_bookmark_blue_3,
        R.drawable.ic_admission_badge_3,
    ),
    COLOR_INVALID(
        R.color.starsColorInvalid,
        R.drawable.bg_person_overview_invalid,
        R.drawable.bg_certificate_grey,
        R.drawable.ic_bookmark,
        R.drawable.ic_admission_badge_1,
    ),
    COLOR_UNDEFINED(
        R.color.starsColorInvalid,
        R.drawable.bg_person_overview_invalid,
        R.drawable.bg_certificate_grey,
        R.drawable.ic_bookmark,
        R.drawable.ic_admission_badge_1,
    ),
    GREEN(
        R.color.starsGreenColor,
        R.drawable.bg_person_overview_green,
        R.drawable.bg_certificate_blue_1,
        R.drawable.ic_bookmark_green,
        R.drawable.ic_admission_badge_green,
    );

    @DrawableRes val defaultCertificateBg: Int = R.drawable.bg_certificate_grey

    companion object {
        /**
         * Returns color of person certificates based on its position in the list
         */
        fun shadeFor(index: Int): PersonColorShade {
            val values = values()
            // Excludes COLOR_INVALID, COLOR_UNDEFINED
            return values.getOrElse(index.rem(values.size - 3)) { COLOR_1 }
        }
    }
}
