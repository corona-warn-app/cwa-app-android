package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard.Item
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.VaccinationCertificateCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class VaccinationCertificateCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<Item, VaccinationCertificateCardBinding>(
        layoutRes = R.layout.vaccination_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationCertificateCardBinding> = lazy {
        VaccinationCertificateCardBinding.bind(itemView)
    }
    override val onBindData: VaccinationCertificateCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }
        vaccinationDosesInfo.text = context.getString(
            R.string.vaccination_certificate_doses,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
        certificateDate.text = context.getString(
            R.string.vaccination_certificate_vaccinated_on,
            certificate.vaccinatedAt.toShortDayFormat()
        )
        currentCertificate.isVisible = curItem.isCurrentCertificate
        val icon = when {
            // Final shot
            certificate.isFinalShot -> when (curItem.status) {
                IMMUNITY -> R.drawable.ic_vaccination_immune
                else -> R.drawable.ic_vaccination_complete
            }

            // Other shots
            else -> R.drawable.ic_vaccination_incomplete
        }
        val background = when {
            curItem.isCurrentCertificate -> curItem.colorShade.currentCertificateBg
            else -> curItem.colorShade.defaultCertificateBg
        }
        certificateBg.setImageResource(background)
        certificateIcon.setImageResource(icon)
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val isCurrentCertificate: Boolean,
        val status: VaccinatedPerson.Status,
        val colorShade: PersonColorShade,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.certificateId.hashCode().toLong()
    }
}
