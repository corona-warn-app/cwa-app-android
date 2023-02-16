package de.rki.coronawarnapp.util

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Expired
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.ExpiringSoon
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun PersonOverviewItemBinding.setUIState(
    item: PersonCertificateCard.Item
) {
    val certificateItems = item.overviewCertificates.take(3)
    val firstCertificate = certificateItems.first()
    val secondCertificate = certificateItems.getOrNull(1)
    val thirdCertificate = certificateItems.getOrNull(2)

    val context = root.context
    val valid = firstCertificate.cwaCertificate.isDisplayValid
    val color = item.colorShade
    val badgeCount = item.badgeCount
    backgroundImage.setImageResource(color.background)
    starsImage.setImageDrawable(starsDrawable(context, color))
    name.text = firstCertificate.cwaCertificate.fullName
    certificateBadgeCount.isVisible = badgeCount != 0
    certificateBadgeCount.text = badgeCount.toString()
    certificateBadgeText.isVisible = badgeCount != 0

    qrCodeCard.apply {
        loadQrImage(firstCertificate.cwaCertificate)
        setGStatusBadge(item, color)
        setMaskBadge(item, color)
        setVisibility(valid)
        bindButtonToggleGroup(secondCertificate, thirdCertificate, firstCertificate, item)
        setOnClickListener { item.onCovPassInfoAction }
    }

    when (firstCertificate.cwaCertificate.displayedState()) {
        is Expired -> updateExpirationViews(
            badgeCount,
            verticalBias = 1.0f,
            expirationText = R.string.certificate_qr_expired
        )

        is Invalid -> updateExpirationViews(
            badgeCount,
            expirationText = R.string.certificate_qr_invalid_signature
        )

        Blocked, Revoked -> updateExpirationViews(
            badgeCount,
            expirationText = R.string.error_dcc_in_blocklist_title
        )

        else -> updateExpirationViews()
    }
}

private fun PersonOverviewItemBinding.updateExpirationViews(
    badgeCount: Int = 1,
    verticalBias: Float = 0f,
    expirationText: Int = 0
) {
    val context = root.context
    statusIcon.isVisible = badgeCount == 0
    statusIcon.constraintLayoutParams.verticalBias = verticalBias
    statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
    statusTitle.isVisible = badgeCount == 0
    if (expirationText != 0) {
        statusTitle.text = context.getText(expirationText)
    }
}

private fun starsDrawable(context: Context, colorShade: PersonColorShade) =
    context.resources.mutateDrawable(
        R.drawable.ic_eu_stars_blue,
        context.getColorCompat(colorShade.starsTint)
    )

fun TextView.displayExpirationState(certificate: CwaCovidCertificate) {
    when (certificate.displayedState()) {
        is ExpiringSoon -> {
            isVisible = true
            text = context.getString(
                R.string.certificate_person_details_card_expiration,
                certificate.headerExpiresAt.toLocalDateTimeUserTz()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                certificate.headerExpiresAt.toLocalDateTimeUserTz()
                    .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            )
        }

        is Expired -> {
            isVisible = true
            text = context.getText(R.string.certificate_qr_expired)
        }

        is Invalid -> {
            isVisible = true
            text = context.getText(R.string.certificate_qr_invalid_signature)
        }

        is Valid -> {
            isVisible = false
            if (certificate.isNew) {
                isVisible = true
                text = context.getText(R.string.test_certificate_qr_new)
            }
        }

        Blocked, Revoked -> {
            isVisible = true
            text = context.getText(R.string.error_dcc_in_blocklist_title)
        }

        CwaCovidCertificate.State.Recycled -> Unit
    }
}

fun CwaCovidCertificate.getEuropaStarsTint(colorShade: PersonColorShade): Int {
    return when {
        colorShade != PersonColorShade.COLOR_UNDEFINED -> colorShade.starsTint
        isDisplayValid -> R.color.starsColor1
        else -> R.color.starsColorInvalid
    }
}

fun CwaCovidCertificate.expendedImageResource(colorShade: PersonColorShade): Int {
    return when {
        colorShade != PersonColorShade.COLOR_UNDEFINED -> colorShade.background
        isDisplayValid -> R.drawable.certificate_complete_gradient
        else -> R.drawable.vaccination_incomplete
    }
}

val View.constraintLayoutParams get() = layoutParams as ConstraintLayout.LayoutParams

/**
 * Display state is just for UI purpose only and does change the state for Test Certificate only
 */
fun CwaCovidCertificate.displayedState(): CwaCovidCertificate.State =
    when (this) {
        is TestCertificate -> if (isDisplayValid) Valid(headerExpiresAt) else state
        else -> state
    }
