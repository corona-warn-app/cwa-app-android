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
    @DrawableRes val maskIcon: Int = R.drawable.ic_mask,
    @DrawableRes val maskLargeBadgeBg: Int = R.drawable.mask_badge_bg,
    @DrawableRes val maskSmallBadge: Int = R.drawable.mask_small_badge,
    @DrawableRes val noMaskSmallBadge: Int = R.drawable.no_mask_small_badge
) {

    COLOR_1(
        starsTint = R.color.starsColor1,
        background = R.drawable.bg_person_overview_1,
        currentCertificateBg = R.drawable.bg_certificate_blue_1,
        bookmarkIcon = R.drawable.ic_bookmark_blue_1,
        admissionBadgeBg = R.drawable.ic_admission_badge_1,
    ),
    COLOR_2(
        starsTint = R.color.starsColor2,
        background = R.drawable.bg_person_overview_2,
        currentCertificateBg = R.drawable.bg_certificate_blue_2,
        bookmarkIcon = R.drawable.ic_bookmark_blue_2,
        admissionBadgeBg = R.drawable.ic_admission_badge_2,
    ),
    COLOR_3(
        starsTint = R.color.starsColor3,
        background = R.drawable.bg_person_overview_3,
        currentCertificateBg = R.drawable.bg_certificate_blue_3,
        bookmarkIcon = R.drawable.ic_bookmark_blue_3,
        admissionBadgeBg = R.drawable.ic_admission_badge_3,
    ),
    COLOR_INVALID(
        starsTint = R.color.starsColorInvalid,
        background = R.drawable.bg_person_overview_invalid,
        currentCertificateBg = R.drawable.bg_certificate_grey,
        bookmarkIcon = R.drawable.ic_bookmark,
        admissionBadgeBg = R.drawable.ic_admission_badge_1,
    ),
    COLOR_UNDEFINED(
        starsTint = R.color.starsColorInvalid,
        background = R.drawable.bg_person_overview_invalid,
        currentCertificateBg = R.drawable.bg_certificate_grey,
        bookmarkIcon = R.drawable.ic_bookmark,
        admissionBadgeBg = R.drawable.ic_admission_badge_1,
    ),
    GREEN(
        starsTint = R.color.starsGreenColor,
        background = R.drawable.bg_person_overview_green,
        currentCertificateBg = R.drawable.bg_certificate_blue_1,
        bookmarkIcon = R.drawable.ic_bookmark_green,
        admissionBadgeBg = R.drawable.ic_admission_badge_green,
        maskIcon = R.drawable.ic_no_mask,
        maskLargeBadgeBg = R.drawable.no_mask_badge_bg,
        maskSmallBadge = R.drawable.no_mask_small_badge,
    );

    @DrawableRes val defaultCertificateBg: Int = R.drawable.bg_certificate_grey

    companion object {
        /**
         * Returns color of person certificates based on its position in the list
         */
        fun shadeFor(index: Int): PersonColorShade {
            val values = values()
            // Excludes COLOR_INVALID, COLOR_UNDEFINED, GREEN from position based colouring
            // these colours appear on specific conditions of person's certificates
            return values.getOrElse(index.rem(values.size - 3)) { COLOR_1 }
        }

        fun colorForState(
            validCertificate: Boolean,
            isMaskOptional: Boolean,
            currentColor: PersonColorShade
        ): PersonColorShade = when {
            isMaskOptional -> GREEN
            validCertificate -> currentColor
            else -> COLOR_INVALID
        }
    }
}
