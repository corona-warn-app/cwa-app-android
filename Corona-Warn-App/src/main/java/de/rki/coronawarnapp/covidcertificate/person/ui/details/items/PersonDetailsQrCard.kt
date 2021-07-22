package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import coil.loadAny
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.PersonDetailsQrCardItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import timber.log.Timber
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

class PersonDetailsQrCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<PersonDetailsQrCard.Item, PersonDetailsQrCardItemBinding>(
        layoutRes = R.layout.person_details_qr_card_item,
        parent = parent
    ) {
    override val viewBinding: Lazy<PersonDetailsQrCardItemBinding> = lazy {
        PersonDetailsQrCardItemBinding.bind(itemView)
    }

    override val onBindData: PersonDetailsQrCardItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        curItem.apply {
            image.loadAny(
                CoilQrCode(content = curItem.certificate.qrCode)
            ) {
                crossfade(true)
                loadingView(image, progressBar)
            }

            startValidationCheckButton.defaultButton.setOnClickListener {
                validateCertificate(certificate.containerId)
            }
            startValidationCheckButton.isLoading = curItem.isLoading
            when (certificate) {
                is TestCertificate -> {
                    val dateTime = certificate.sampleCollectedAt.toUserTimeZone().run {
                        "${toShortDayFormat()}, ${toShortTimeFormat()}"
                    }

                    qrTitle.text = context.getString(R.string.test_certificate_name)
                    qrSubtitle.text = context.getString(R.string.test_certificate_qrcode_card_sampled_on, dateTime)
                }
                is VaccinationCertificate -> {
                    qrTitle.text = context.getString(R.string.vaccination_details_subtitle)
                    qrSubtitle.text = context.getString(
                        R.string.vaccination_certificate_vaccinated_on,
                        certificate.vaccinatedOn.toShortDayFormat()
                    )
                }
                is RecoveryCertificate -> {
                    qrTitle.text = context.getString(R.string.recovery_certificate_name)
                    qrSubtitle.text = context.getString(
                        R.string.recovery_certificate_valid_until,
                        certificate.validUntil.toShortDayFormat()
                    )
                }
            }

            when (certificate.getState()) {
                is CwaCovidCertificate.State.ExpiringSoon -> {
                    expirationStatusIcon.visibility = View.VISIBLE
                    expirationStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_av_timer))
                    expirationStatusText.visibility = View.VISIBLE
                    expirationStatusText.text = context.getString(
                        R.string.certificate_qr_expiration,
                        curItem.certificate.headerExpiresAt.toShortDayFormat(),
                        curItem.certificate.headerExpiresAt.toShortTimeFormat()
                    )
                }

                is CwaCovidCertificate.State.Expired -> {
                    expirationStatusIcon.visibility = View.VISIBLE
                    expirationStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_outline))
                    expirationStatusText.visibility = View.VISIBLE
                    expirationStatusText.text = context.getText(R.string.certificate_qr_expired)
                }

                is CwaCovidCertificate.State.Invalid -> {
                    expirationStatusIcon.visibility = View.VISIBLE
                    expirationStatusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error_outline))
                    expirationStatusText.visibility = View.VISIBLE
                    expirationStatusText.text = context.getText(R.string.certificate_qr_invalid_signature)
                }

                else -> {
                }
            }
        }
    }

    data class Item(
        val certificate: CwaCovidCertificate,
        val isLoading: Boolean,
        val validateCertificate: (CertificateContainerId) -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
