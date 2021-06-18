package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.details.items.VaccinationCertificateCard.Item
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.COMPLETE
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.IMMUNITY
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson.Status.INCOMPLETE
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.VaccinationCertificateCardBinding
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
        vaccinationCardTitle.text = context.getString(
            R.string.vaccination_list_vaccination_card_title,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
        vaccinationCardSubtitle.text = context.getString(
            R.string.vaccination_list_vaccination_card_subtitle,
            certificate.vaccinatedAt
        )

        val iconRes = when (curItem.vaccinationStatus) {
            INCOMPLETE,
            COMPLETE -> when {
                certificate.isFinalShot -> R.drawable.ic_vaccination_complete
                else -> R.drawable.ic_vaccination_incomplete
            }
            IMMUNITY -> when {
                certificate.isFinalShot -> R.drawable.ic_vaccination_immune
                else -> R.drawable.ic_vaccination_incomplete
            }
        }
        vaccinationIcon.setImageResource(iconRes)
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val vaccinationStatus: VaccinatedPerson.Status,
        val onClick: () -> Unit
    ) : SpecificCertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.certificateId.hashCode().toLong()
    }
}
