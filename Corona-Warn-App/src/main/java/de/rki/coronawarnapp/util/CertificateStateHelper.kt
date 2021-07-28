package de.rki.coronawarnapp.util

import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.IncludeCertificateQrcodeCardBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone

fun IncludeCertificateQrcodeCardBinding.bindValidityViews(
    certificate: CwaCovidCertificate,
    isPersonOverview: Boolean = false,
    isPersonDetails: Boolean = false,
    isCertificateDetails: Boolean = false
) {
    val valid = certificate.isValid
    val context = root.context

    invalidOverlay.isGone = valid
    image.isEnabled = isCertificateDetails && valid // Disable Qr-Code image from opening full-screen mode

    when (certificate) {
        is TestCertificate -> {
            val dateTime = certificate.sampleCollectedAt.toUserTimeZone().run {
                "${toShortDayFormat()}, ${toShortTimeFormat()}"
            }
            qrTitle.isVisible = !isPersonOverview
            qrTitle.text = context.getString(R.string.test_certificate_name)
            qrSubtitle.isVisible = !isPersonOverview
            qrSubtitle.text = context.getString(R.string.test_certificate_qrcode_card_sampled_on, dateTime)
        }
        is VaccinationCertificate -> {
            qrTitle.isVisible = !isPersonOverview
            qrTitle.text = context.getString(R.string.vaccination_details_subtitle)
            qrSubtitle.isVisible = !isPersonOverview
            qrSubtitle.text = context.getString(
                R.string.vaccination_certificate_vaccinated_on,
                certificate.vaccinatedOn.toShortDayFormat()
            )
        }
        is RecoveryCertificate -> {
            qrTitle.isVisible = !isPersonOverview
            qrTitle.text = context.getString(R.string.recovery_certificate_name)
            qrSubtitle.isVisible = !isPersonOverview
            qrSubtitle.text = context.getString(
                R.string.recovery_certificate_valid_until,
                certificate.validUntil.toShortDayFormat()
            )
        }
    }
    when (certificate.getState()) {
        is CwaCovidCertificate.State.ExpiringSoon -> {
            expirationStatusIcon.isVisible = true
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_av_timer))
            expirationStatusText.isVisible = true
            expirationStatusText.text = context.getString(
                R.string.certificate_qr_expiration,
                certificate.headerExpiresAt.toShortDayFormat(),
                certificate.headerExpiresAt.toShortTimeFormat()
            )
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.expiration_info)
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Expired -> {
            expirationStatusIcon.isVisible = true
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            expirationStatusText.isVisible = true
            expirationStatusText.text = context.getText(R.string.certificate_qr_expired)
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.expired_certificate_info)
            qrSubtitle.isVisible = false
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Invalid -> {
            expirationStatusIcon.isVisible = true
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            expirationStatusText.isVisible = true
            expirationStatusText.text = context.getText(R.string.certificate_qr_invalid_signature)
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.invalid_certificate_signature_info)
            qrSubtitle.isVisible = false
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Valid -> {
            expirationStatusIcon.isVisible = false
            expirationStatusText.isVisible = false
            expirationStatusBody.isVisible = false
            qrTitle.isVisible = isPersonDetails
            qrSubtitle.isVisible = isPersonDetails
            startValidationCheckButton.isVisible = isPersonDetails
        }
    }
}

fun TextView.displayExpirationState(certificate: CwaCovidCertificate) {
    when (certificate.getState()) {
        is CwaCovidCertificate.State.ExpiringSoon -> {
            isVisible = true
            text = context.getString(
                R.string.certificate_person_details_card_expiration,
                certificate.headerExpiresAt.toShortDayFormat(),
                certificate.headerExpiresAt.toShortTimeFormat()
            )
        }

        is CwaCovidCertificate.State.Expired -> {
            isVisible = true
            text = context.getText(R.string.certificate_qr_expired)
        }

        is CwaCovidCertificate.State.Invalid -> {
            isVisible = true
            text = context.getText(R.string.certificate_qr_invalid_signature)
        }

        is CwaCovidCertificate.State.Valid -> {
            isVisible = false
        }
    }
}

val CwaCovidCertificate.europaStarsResource
    get() = when {
        isValid -> R.drawable.ic_eu_stars_blue
        else -> R.drawable.ic_eu_stars_grey
    }

val CwaCovidCertificate.expendedImageResource
    get() = when {
        isValid -> R.drawable.certificate_complete_gradient
        else -> R.drawable.vaccination_incomplete
    }
