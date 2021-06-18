package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.PersonDetailsQrCardItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PersonDetailsQrCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<PersonDetailsQrCard.Item, PersonDetailsQrCardItemBinding>(
        layoutRes = R.layout.include_certificate_qrcode_card,
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
        image.setImageBitmap(curItem.qrCodeBitmap)
        curItem.apply {
            qrCodeBitmap?.let { progressBar.hide() }
            qrTitle.isVisible = true
            qrSubtitle.isVisible = true
            when (certificate) {
                is TestCertificate -> {
                    qrTitle.text = context.getString(R.string.detail_green_certificate_card_title)
                    qrSubtitle.text = context.getString(
                        R.string.test_certificate_sampled_on,
                        certificate.sampleCollectedAt.toShortDayFormat()
                    )
                }
                is VaccinationCertificate -> {
                    qrTitle.text = context.getString(R.string.vaccination_details_subtitle)
                    qrSubtitle.text = context.getString(
                        R.string.vaccination_certificate_vaccinated_on,
                        certificate.vaccinatedAt.toString(DATE_FORMAT)
                    )
                }
                is RecoveryCertificate -> {
                    qrTitle.text = context.getString(R.string.recovery_certificate_title)
                    qrSubtitle.text = context.getString(
                        R.string.recovery_certificate_valid_until,
                        certificate.validUntil.toString(DATE_FORMAT)
                    )
                }
            }
        }
    }

    companion object {
        private const val DATE_FORMAT = "dd.MM.yy"
    }

    data class Item(
        val certificate: CwaCovidCertificate,
        val qrCodeBitmap: Bitmap?
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
