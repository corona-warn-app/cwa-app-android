package de.rki.coronawarnapp.util

import android.content.Context
import android.view.View
import android.widget.TextView
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

object CertificateStateHelper {

    fun IncludeCertificateQrcodeCardBinding.bindValidityViews(
        context: Context,
        certificate: CwaCovidCertificate,
        isPersonOverview: Boolean = false,
        isPersonDetails: Boolean = false,
        isCertificateDetails: Boolean = false
    ) {
        when (certificate) {
            is TestCertificate -> {
                val dateTime = certificate.sampleCollectedAt.toUserTimeZone().run {
                    "${toShortDayFormat()}, ${toShortTimeFormat()}"
                }
                qrTitle.visibility = setVisibility(!isPersonOverview)
                qrTitle.text = context.getString(R.string.test_certificate_name)
                qrSubtitle.visibility = setVisibility(!isPersonOverview)
                qrSubtitle.text = context.getString(R.string.test_certificate_qrcode_card_sampled_on, dateTime)
            }
            is VaccinationCertificate -> {
                qrTitle.visibility = setVisibility(!isPersonOverview)
                qrTitle.text = context.getString(R.string.vaccination_details_subtitle)
                qrSubtitle.visibility = setVisibility(!isPersonOverview)
                qrSubtitle.text = context.getString(
                    R.string.vaccination_certificate_vaccinated_on,
                    certificate.vaccinatedOn.toShortDayFormat()
                )
            }
            is RecoveryCertificate -> {
                qrTitle.visibility = setVisibility(!isPersonOverview)
                qrTitle.text = context.getString(R.string.recovery_certificate_name)
                qrSubtitle.visibility = setVisibility(!isPersonOverview)
                qrSubtitle.text = context.getString(
                    R.string.recovery_certificate_valid_until,
                    certificate.validUntil.toShortDayFormat()
                )
            }
        }
        when (certificate.getState()) {
            is CwaCovidCertificate.State.ExpiringSoon -> {
                expirationStatusIcon.visibility = View.VISIBLE
                expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_av_timer))
                expirationStatusText.visibility = View.VISIBLE
                expirationStatusText.text = context.getString(
                    R.string.certificate_qr_expiration,
                    certificate.headerExpiresAt.toShortDayFormat(),
                    certificate.headerExpiresAt.toShortTimeFormat()
                )
                expirationStatusBody.visibility = setVisibility(isCertificateDetails)
                expirationStatusBody.text = context.getText(R.string.expiration_info)
                startValidationCheckButton.visibility = setVisibility(isPersonDetails)
            }

            is CwaCovidCertificate.State.Expired -> {
                expirationStatusIcon.visibility = View.VISIBLE
                expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
                expirationStatusText.visibility = View.VISIBLE
                expirationStatusText.text = context.getText(R.string.certificate_qr_expired)
                expirationStatusBody.visibility = setVisibility(isCertificateDetails)
                expirationStatusBody.text = context.getText(R.string.expired_certificate_info)
                qrSubtitle.visibility = View.GONE
                startValidationCheckButton.visibility = setVisibility(isPersonDetails)
            }

            is CwaCovidCertificate.State.Invalid -> {
                expirationStatusIcon.visibility = View.VISIBLE
                expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
                expirationStatusText.visibility = View.VISIBLE
                expirationStatusText.text = context.getText(R.string.certificate_qr_invalid_signature)
                expirationStatusBody.visibility = setVisibility(isCertificateDetails)
                expirationStatusBody.text = context.getText(R.string.invalid_certificate_signature_info)
                qrSubtitle.visibility = View.GONE
                startValidationCheckButton.visibility = setVisibility(isPersonDetails)
            }

            is CwaCovidCertificate.State.Valid -> {
                expirationStatusIcon.visibility = View.GONE
                expirationStatusText.visibility = View.GONE
                expirationStatusBody.visibility = View.GONE
                qrTitle.visibility = setVisibility(isPersonDetails)
                qrSubtitle.visibility = setVisibility(isPersonDetails)
                startValidationCheckButton.visibility = setVisibility(isPersonDetails)
            }
        }
    }

    private fun setVisibility(visibility: Boolean) =
        if (visibility) {
            View.VISIBLE
        } else {
            View.GONE
        }

    fun displayIndividualCardsExpirationState(
        certificateExpiration: TextView,
        context: Context,
        certificate: CwaCovidCertificate
    ) {
        when (certificate.getState()) {
            is CwaCovidCertificate.State.ExpiringSoon -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getString(
                    R.string.certificate_person_details_card_expiration,
                    certificate.headerExpiresAt.toShortDayFormat(),
                    certificate.headerExpiresAt.toShortTimeFormat()
                )
            }

            is CwaCovidCertificate.State.Expired -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getText(R.string.certificate_qr_expired)
            }

            is CwaCovidCertificate.State.Invalid -> {
                certificateExpiration.visibility = View.VISIBLE
                certificateExpiration.text = context.getText(R.string.certificate_qr_invalid_signature)
            }

            else -> {
                certificateExpiration.visibility = View.GONE
            }
        }
    }
}
