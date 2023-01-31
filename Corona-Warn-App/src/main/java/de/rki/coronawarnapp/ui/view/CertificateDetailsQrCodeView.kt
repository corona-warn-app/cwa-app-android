package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.CertificateDetailsQrCodeLayoutBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.constraintLayoutParams
import de.rki.coronawarnapp.util.displayedState
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CertificateDetailsQrCodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: CertificateDetailsQrCodeLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.certificate_details_qr_code_layout, this, true)
        binding = CertificateDetailsQrCodeLayoutBinding.bind(this)
    }

    fun setQrImage(request: CoilQrCode, onClick: () -> Unit) {
        with(binding) {
            image.loadAny(request) {
                crossfade(true)
                loadingView(image, progressBar)
            }
            image.setOnClickListener { onClick() }
        }
    }

    fun hideProgressBar() {
        binding.progressBar.hide()
    }

    fun bindValidityViews(
        certificate: CwaCovidCertificate,
        onCovPassInfoAction: () -> Unit
    ) {
        with(binding) {
            val valid = certificate.isDisplayValid
            val context = root.context
            covpassInfoTitle.isVisible = valid
            covpassInfoButton.isVisible = valid
            covpassInfoButton.setOnClickListener { onCovPassInfoAction() }

            val isActualQrCodeVisible = valid || certificate.state is CwaCovidCertificate.State.Blocked
            invalidOverlay.isGone = isActualQrCodeVisible
            image.isEnabled = isActualQrCodeVisible // Disable Qr-Code full-screen mode

            statusGroup.isGone = certificate.displayedState() is CwaCovidCertificate.State.Valid

            when (certificate.displayedState()) {
                is CwaCovidCertificate.State.ExpiringSoon -> {
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

                is CwaCovidCertificate.State.Expired -> {
                    setQrTitle(certificate, qrTitle, context)
                    notificationBadge.isVisible = false
                    statusIcon.constraintLayoutParams.verticalBias = 1.0f
                    statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
                    statusTitle.text = context.getText(R.string.certificate_qr_expired)
                    statusBody.text = context.getText(R.string.expiration_info)
                }

                is CwaCovidCertificate.State.Invalid -> {
                    setQrTitle(certificate, qrTitle, context)
                    notificationBadge.isVisible = certificate.hasNotificationBadge
                    statusIcon.constraintLayoutParams.verticalBias = 0f
                    statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
                    statusTitle.text = context.getText(R.string.certificate_qr_invalid_signature)
                    statusBody.text = context.getText(R.string.invalid_certificate_signature_info)
                }

                CwaCovidCertificate.State.Blocked, CwaCovidCertificate.State.Revoked -> {
                    setQrTitle(certificate, qrTitle, context)
                    notificationBadge.isVisible = certificate.hasNotificationBadge
                    statusIcon.constraintLayoutParams.verticalBias = 0f
                    statusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
                    statusTitle.text = context.getText(R.string.error_dcc_in_blocklist_title)
                    statusBody.text = context.getText(
                        messageForScreenedCert(certificate)
                    )
                }

                is CwaCovidCertificate.State.Valid,
                CwaCovidCertificate.State.Recycled -> Unit
            }
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
}
