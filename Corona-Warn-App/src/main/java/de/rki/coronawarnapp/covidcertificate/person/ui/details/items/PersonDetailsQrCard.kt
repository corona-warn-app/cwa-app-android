package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.PersonDetailsQrCardItemBinding
import de.rki.coronawarnapp.util.QrCodeHelper
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
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

        var qrCodeString = curItem.certificate.qrCode
        if(QrCodeHelper.isInvalidOrExpired(curItem.certificate.getState())) {
            qrCodeString = QrCodeHelper.sampleQrCodeText
        }



        curItem.apply {
            image.loadAny(
                CoilQrCode(content = qrCodeString)
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

            if (QrCodeHelper.isInvalidOrExpired(certificate.getState())) {
                image.alpha = 0.1f
                invalidQrCodeSymbol.isVisible = true
            } else {
                invalidQrCodeSymbol.isVisible = false
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
