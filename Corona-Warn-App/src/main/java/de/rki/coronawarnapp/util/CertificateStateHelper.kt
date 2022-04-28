package de.rki.coronawarnapp.util

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import coil.loadAny
import com.google.android.material.button.MaterialButton
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.common.setOnCheckedChangeListener
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Blocked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Expired
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.ExpiringSoon
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Invalid
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Revoked
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate.State.Valid
import de.rki.coronawarnapp.covidcertificate.common.certificate.getValidQrCode
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.IncludeCertificateOverviewQrCardBinding
import de.rki.coronawarnapp.databinding.IncludeCertificateQrcodeCardBinding
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.coil.loadingView

fun IncludeCertificateQrcodeCardBinding.bindValidityViews(
    certificate: CwaCovidCertificate,
    onCovPassInfoAction: () -> Unit
) {
    val valid = certificate.isDisplayValid
    val context = root.context
    covpassInfoTitle.isVisible = valid
    covpassInfoButton.isVisible = valid
    covpassInfoButton.setOnClickListener { onCovPassInfoAction() }

    val isActualQrCodeVisible = valid || certificate.state is Blocked
    invalidOverlay.isGone = isActualQrCodeVisible
    image.isEnabled = isActualQrCodeVisible // Disable Qr-Code full-screen mode

    statusGroup.isGone = certificate.displayedState() is Valid

    when (certificate.displayedState()) {
        is ExpiringSoon -> {
            statusIcon.constraintLayoutParams.verticalBias = 0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_av_timer))
            statusTitle.text = context.getString(
                R.string.certificate_qr_expiration,
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortDayFormat(),
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortTimeFormat()
            )
            statusBody.text = context.getText(R.string.expiration_info)
        }

        is Expired -> {
            statusIcon.constraintLayoutParams.verticalBias = 1.0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            statusTitle.text = context.getText(R.string.certificate_qr_expired)
            statusBody.text = context.getText(R.string.expired_certificate_info)
        }

        is Invalid -> {
            statusIcon.constraintLayoutParams.verticalBias = 0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            statusTitle.text = context.getText(R.string.certificate_qr_invalid_signature)
            statusBody.text = context.getText(R.string.invalid_certificate_signature_info)
        }

        Blocked, Revoked -> {
            statusIcon.constraintLayoutParams.verticalBias = 0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            statusTitle.text = context.getText(R.string.error_dcc_in_blocklist_title)
            statusBody.text = context.getText(
                messageForScreenedCert(certificate)
            )
        }
        is Valid,
        CwaCovidCertificate.State.Recycled -> Unit
    }
}

private fun messageForScreenedCert(certificate: CwaCovidCertificate) =
    when (certificate.dccData.header.issuer) {
        "DE" -> R.string.dcc_screened_de_message
        else -> R.string.dcc_screened_foreign_message
    }

@Suppress("LongParameterList", "ComplexMethod")
fun PersonOverviewItemBinding.setUIState(
    certificateItems: List<PersonCertificateCard.Item.OverviewCertificate>,
    colorShade: PersonColorShade,
    statusBadgeText: String = "",
    badgeCount: Int = 0,
    onCovPassInfoAction: () -> Unit
) {
    val firstCertificate = certificateItems.first()
    val secondCertificate = certificateItems.getOrNull(1)
    val thirdCertificate = certificateItems.getOrNull(2)

    fun setButton(
        button: MaterialButton,
        certificate: PersonCertificateCard.Item.OverviewCertificate?,
        typeface: Typeface = Typeface.DEFAULT,
    ) {
        if (certificate?.buttonText == null) {
            button.isGone = true
        } else {
            button.typeface = typeface
            button.text = certificate.buttonText
            button.isGone = false
        }
    }

    val context = root.context
    val valid = firstCertificate.cwaCertificate.isDisplayValid
    val color = when {
        valid -> colorShade
        else -> PersonColorShade.COLOR_INVALID
    }

    backgroundImage.setImageResource(color.background)
    starsImage.setImageDrawable(starsDrawable(context, color))
    name.text = firstCertificate.cwaCertificate.fullName
    certificateBadgeCount.isVisible = badgeCount != 0
    certificateBadgeCount.text = badgeCount.toString()
    certificateBadgeText.isVisible = badgeCount != 0
    qrCodeCard.apply {
        loadQrImage(firstCertificate.cwaCertificate)
        statusText.isVisible = statusBadgeText.isNotEmpty()
        statusBadge.isVisible = statusBadgeText.isNotEmpty()
        if (statusBadgeText.isNotEmpty()) {
            statusBadge.text = statusBadgeText
        }
        covpassInfoTitle.isVisible = valid
        covpassInfoButton.isVisible = valid
        covpassInfoButton.setOnClickListener { onCovPassInfoAction() }
        invalidOverlay.isGone = valid
        image.isEnabled = valid
        certificateToggleGroup.isVisible = secondCertificate != null || thirdCertificate != null

        setButton(firstCertificateButton, firstCertificate, Typeface.DEFAULT_BOLD)
        setButton(secondCertificateButton, secondCertificate)
        setButton(thirdCertificateButton, thirdCertificate)

        certificateToggleGroup.setOnCheckedChangeListener { checkedId ->
            changeQrCodeOnButtonPress(
                checkedId,
                firstCertificate.cwaCertificate,
                secondCertificate?.cwaCertificate,
                thirdCertificate?.cwaCertificate
            )
        }
        certificateToggleGroup.check(0)
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

private fun IncludeCertificateOverviewQrCardBinding.changeQrCodeOnButtonPress(
    checkedId: Int?,
    firstCertificate: CwaCovidCertificate,
    secondCertificate: CwaCovidCertificate?,
    thirdCertificate: CwaCovidCertificate?
) {
    when (checkedId) {
        R.id.first_certificate_button -> {
            loadQrImage(firstCertificate)
            firstCertificateButton.typeface = Typeface.DEFAULT_BOLD
            secondCertificateButton.typeface = Typeface.DEFAULT
            thirdCertificateButton.typeface = Typeface.DEFAULT
        }
        R.id.second_certificate_button -> {
            loadQrImage(secondCertificate)
            firstCertificateButton.typeface = Typeface.DEFAULT
            secondCertificateButton.typeface = Typeface.DEFAULT_BOLD
            thirdCertificateButton.typeface = Typeface.DEFAULT
        }
        R.id.third_certificate_button -> {
            loadQrImage(thirdCertificate)
            firstCertificateButton.typeface = Typeface.DEFAULT
            secondCertificateButton.typeface = Typeface.DEFAULT
            thirdCertificateButton.typeface = Typeface.DEFAULT_BOLD
        }
    }
}

private fun IncludeCertificateOverviewQrCardBinding.loadQrImage(certificate: CwaCovidCertificate?) {
    image.loadAny(certificate?.getValidQrCode()) {
        crossfade(true)
        loadingView(image, progressBar)
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
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortDayFormat(),
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortTimeFormat()
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

private val View.constraintLayoutParams get() = layoutParams as ConstraintLayout.LayoutParams

/**
 * Display state is just for UI purpose only and does change the state for Test Certificate only
 */
private fun CwaCovidCertificate.displayedState(): CwaCovidCertificate.State =
    when (this) {
        is TestCertificate -> if (isDisplayValid) Valid(headerExpiresAt) else state
        else -> state
    }
