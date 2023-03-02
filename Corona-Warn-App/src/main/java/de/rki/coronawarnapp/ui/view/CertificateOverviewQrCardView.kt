package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import coil.loadAny
import com.google.android.material.button.MaterialButton
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.common.setOnCheckedChangeListener
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.getValidQrCode
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.databinding.CertificateOverviewQrCardLayoutBinding
import de.rki.coronawarnapp.util.coil.loadingView

class CertificateOverviewQrCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: CertificateOverviewQrCardLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.certificate_overview_qr_card_layout, this, true)
        binding = CertificateOverviewQrCardLayoutBinding.bind(this)
    }

    fun bindButtonToggleGroup(
        secondCertificate: PersonCertificateCard.Item.OverviewCertificate?,
        thirdCertificate: PersonCertificateCard.Item.OverviewCertificate?,
        firstCertificate: PersonCertificateCard.Item.OverviewCertificate,
        item: PersonCertificateCard.Item
    ) {
        with(binding) {
            certificateToggleGroup.isVisible = secondCertificate != null || thirdCertificate != null
            setButton(firstCertificateButton, firstCertificate, Typeface.DEFAULT_BOLD)
            setButton(secondCertificateButton, secondCertificate)
            setButton(thirdCertificateButton, thirdCertificate)

            certificateToggleGroup.setOnCheckedChangeListener { checkedId ->
                item.onCertificateSelected(checkedId.toCertificateSelection())
            }

            when (item.certificateSelection) {
                PersonCertificateCard.Item.CertificateSelection.FIRST -> {
                    loadQrImage(firstCertificate.cwaCertificate)
                    firstCertificateButton.typeface = Typeface.DEFAULT_BOLD
                    secondCertificateButton.typeface = Typeface.DEFAULT
                    thirdCertificateButton.typeface = Typeface.DEFAULT
                }

                PersonCertificateCard.Item.CertificateSelection.SECOND -> {
                    loadQrImage(secondCertificate?.cwaCertificate)
                    firstCertificateButton.typeface = Typeface.DEFAULT
                    secondCertificateButton.typeface = Typeface.DEFAULT_BOLD
                    thirdCertificateButton.typeface = Typeface.DEFAULT
                }

                PersonCertificateCard.Item.CertificateSelection.THIRD -> {
                    loadQrImage(thirdCertificate?.cwaCertificate)
                    firstCertificateButton.typeface = Typeface.DEFAULT
                    secondCertificateButton.typeface = Typeface.DEFAULT
                    thirdCertificateButton.typeface = Typeface.DEFAULT_BOLD
                }
            }

            certificateToggleGroup.check(item.certificateSelection.toCheckId())
        }
    }

    fun loadQrImage(certificate: CwaCovidCertificate?) {
        binding.image.loadAny(certificate?.getValidQrCode()) {
            crossfade(true)
            loadingView(binding.image, binding.progressBar)
        }
    }

    fun setGStatusBadge(
        item: PersonCertificateCard.Item,
        color: PersonColorShade
    ) {
        with(binding) {
            statusBadge.setBackgroundResource(color.admissionBadgeBg)
            statusBadge.text = item.admission.text
            val state = item.admission.state
            statusBadge.visibility = when {
                state == null -> INVISIBLE
                state.visible -> if (item.admission.text.isNotEmpty()) VISIBLE else GONE
                else -> GONE
            }
        }
    }

    fun setMaskBadge(
        item: PersonCertificateCard.Item,
        color: PersonColorShade
    ) {
        with(binding) {
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
    }

    private fun setButton(
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

    fun setVisibility(visibility: Boolean) {
        with(binding) {
            covpassInfoTitle.isVisible = visibility
            covpassInfoButton.isVisible = visibility
            invalidOverlay.isGone = visibility
            image.isEnabled = visibility
        }
    }

    fun setCovPassInfoClickListener(l: OnClickListener?) {
        binding.covpassInfoButton.setOnClickListener(l)
    }

    private fun Int?.toCertificateSelection() = when (this) {
        R.id.first_certificate_button -> PersonCertificateCard.Item.CertificateSelection.FIRST
        R.id.second_certificate_button -> PersonCertificateCard.Item.CertificateSelection.SECOND
        R.id.third_certificate_button -> PersonCertificateCard.Item.CertificateSelection.THIRD
        else -> PersonCertificateCard.Item.CertificateSelection.FIRST
    }

    private fun PersonCertificateCard.Item.CertificateSelection.toCheckId() = when (this) {
        PersonCertificateCard.Item.CertificateSelection.FIRST -> R.id.first_certificate_button
        PersonCertificateCard.Item.CertificateSelection.SECOND -> R.id.second_certificate_button
        PersonCertificateCard.Item.CertificateSelection.THIRD -> R.id.third_certificate_button
    }
}
