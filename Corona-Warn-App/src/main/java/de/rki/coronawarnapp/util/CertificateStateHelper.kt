package de.rki.coronawarnapp.util

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
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
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard.Item.CertificateSelection
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard.Item.OverviewCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.IncludeCertificateOverviewQrCardBinding
import de.rki.coronawarnapp.databinding.IncludeCertificateQrcodeCardBinding
import de.rki.coronawarnapp.databinding.PersonOverviewItemBinding
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.coil.loadingView
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
            setQrTitle(certificate, qrTitle, context)
            notificationBadge.isVisible = false
            statusIcon.constraintLayoutParams.verticalBias = 0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_av_timer))
            statusTitle.text = context.getString(
                R.string.certificate_qr_expiration,
                certificate.headerExpiresAt.toLocalDateTimeUserTz()
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)),
                certificate.headerExpiresAt.toLocalDateTimeUserTz()
                    .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            )
            statusBody.text = context.getText(R.string.expiration_info)
        }

        is Expired -> {
            setQrTitle(certificate, qrTitle, context)
            notificationBadge.isVisible = false
            statusIcon.constraintLayoutParams.verticalBias = 1.0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            statusTitle.text = context.getText(R.string.certificate_qr_expired)
            statusBody.text = context.getText(R.string.expiration_info)
        }

        is Invalid -> {
            setQrTitle(certificate, qrTitle, context)
            notificationBadge.isVisible = certificate.hasNotificationBadge
            statusIcon.constraintLayoutParams.verticalBias = 0f
            statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            statusTitle.text = context.getText(R.string.certificate_qr_invalid_signature)
            statusBody.text = context.getText(R.string.invalid_certificate_signature_info)
        }

        Blocked, Revoked -> {
            setQrTitle(certificate, qrTitle, context)
            notificationBadge.isVisible = certificate.hasNotificationBadge
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

private fun setQrTitle(certificate: CwaCovidCertificate, qrTitle: TextView, context: Context) {
    qrTitle.isVisible = true
    when (certificate) {
        is TestCertificate -> qrTitle.text = context.getString(R.string.test_certificate_name)
        is VaccinationCertificate -> qrTitle.text = context.getString(R.string.vaccination_certificate_name)
        is RecoveryCertificate -> qrTitle.text = context.getString(R.string.recovery_certificate_name)
    }
}

private fun messageForScreenedCert(certificate: CwaCovidCertificate) =
    when (certificate.dccData.header.issuer) {
        "DE" -> R.string.dcc_screened_de_message
        else -> R.string.dcc_screened_foreign_message
    }

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
        setMaskBadge(item, color)
        setGStatusBadge(item, color)

        covpassInfoTitle.isVisible = valid
        covpassInfoButton.isVisible = valid
        covpassInfoButton.setOnClickListener { item.onCovPassInfoAction() }
        invalidOverlay.isGone = valid
        image.isEnabled = valid
        bindButtonToggleGroup(secondCertificate, thirdCertificate, firstCertificate, item)
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

private fun IncludeCertificateOverviewQrCardBinding.setGStatusBadge(
    item: PersonCertificateCard.Item,
    color: PersonColorShade
) {
    statusBadge.setBackgroundResource(color.admissionBadgeBg)
    statusBadge.text = item.admission.text
    val state = item.admission.state
    statusBadge.visibility = when {
        state == null -> INVISIBLE
        state.visible -> if (item.admission.text.isNotEmpty()) VISIBLE else GONE
        else -> GONE
    }
}

private fun IncludeCertificateOverviewQrCardBinding.setMaskBadge(
    item: PersonCertificateCard.Item,
    color: PersonColorShade
) {
    val state = item.mask.state
    maskBadge.visibility = when {
        state == null -> INVISIBLE
        state.visible -> if (item.mask.text.isNotEmpty()) VISIBLE else GONE
        else -> GONE
    }
    maskBadge.text = item.mask.text
    maskBadge.setBackgroundResource(color.maskLargeBadgeBg)
    maskBadge.setCompoundDrawablesWithIntrinsicBounds(color.maskIcon, 0, 0, 0)
    maskBadge.setTextColor(maskBadge.resources.getColor(color.maskBadgeTextColor, null))
}

private fun IncludeCertificateOverviewQrCardBinding.bindButtonToggleGroup(
    secondCertificate: OverviewCertificate?,
    thirdCertificate: OverviewCertificate?,
    firstCertificate: OverviewCertificate,
    item: PersonCertificateCard.Item
) {
    certificateToggleGroup.isVisible = secondCertificate != null || thirdCertificate != null
    setButton(firstCertificateButton, firstCertificate, Typeface.DEFAULT_BOLD)
    setButton(secondCertificateButton, secondCertificate)
    setButton(thirdCertificateButton, thirdCertificate)

    certificateToggleGroup.setOnCheckedChangeListener { checkedId ->
        item.onCertificateSelected(checkedId.toCertificateSelection())
    }

    when (item.certificateSelection) {
        CertificateSelection.FIRST -> {
            loadQrImage(firstCertificate.cwaCertificate)
            firstCertificateButton.typeface = Typeface.DEFAULT_BOLD
            secondCertificateButton.typeface = Typeface.DEFAULT
            thirdCertificateButton.typeface = Typeface.DEFAULT
        }

        CertificateSelection.SECOND -> {
            loadQrImage(secondCertificate?.cwaCertificate)
            firstCertificateButton.typeface = Typeface.DEFAULT
            secondCertificateButton.typeface = Typeface.DEFAULT_BOLD
            thirdCertificateButton.typeface = Typeface.DEFAULT
        }

        CertificateSelection.THIRD -> {
            loadQrImage(thirdCertificate?.cwaCertificate)
            firstCertificateButton.typeface = Typeface.DEFAULT
            secondCertificateButton.typeface = Typeface.DEFAULT
            thirdCertificateButton.typeface = Typeface.DEFAULT_BOLD
        }
    }

    certificateToggleGroup.check(item.certificateSelection.toCheckId())
}

private fun Int?.toCertificateSelection() = when (this) {
    R.id.first_certificate_button -> CertificateSelection.FIRST
    R.id.second_certificate_button -> CertificateSelection.SECOND
    R.id.third_certificate_button -> CertificateSelection.THIRD
    else -> CertificateSelection.FIRST
}

private fun CertificateSelection.toCheckId() = when (this) {
    CertificateSelection.FIRST -> R.id.first_certificate_button
    CertificateSelection.SECOND -> R.id.second_certificate_button
    CertificateSelection.THIRD -> R.id.third_certificate_button
}

private fun IncludeCertificateOverviewQrCardBinding.loadQrImage(certificate: CwaCovidCertificate?) {
    image.loadAny(certificate?.getValidQrCode()) {
        crossfade(true)
        loadingView(image, progressBar)
    }
}

private fun setButton(
    button: MaterialButton,
    certificate: OverviewCertificate?,
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

private val View.constraintLayoutParams get() = layoutParams as ConstraintLayout.LayoutParams

/**
 * Display state is just for UI purpose only and does change the state for Test Certificate only
 */
private fun CwaCovidCertificate.displayedState(): CwaCovidCertificate.State =
    when (this) {
        is TestCertificate -> if (isDisplayValid) Valid(headerExpiresAt) else state
        else -> state
    }
