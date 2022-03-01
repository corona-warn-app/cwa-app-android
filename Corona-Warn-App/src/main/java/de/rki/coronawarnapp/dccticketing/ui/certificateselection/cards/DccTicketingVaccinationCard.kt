package de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.DccTicketingVaccinationCardBinding
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DccTicketingVaccinationCard(parent: ViewGroup) :
    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<DccTicketingVaccinationCard.Item,
        DccTicketingVaccinationCardBinding>(layoutRes = R.layout.dcc_ticketing_vaccination_card, parent = parent) {

    override val viewBinding: Lazy<DccTicketingVaccinationCardBinding> = lazy {
        DccTicketingVaccinationCardBinding.bind(itemView)
    }
    override val onBindData: DccTicketingVaccinationCardBinding.(
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
            certificate.vaccinatedOn?.toShortDayFormat() ?: certificate.rawCertificate.vaccination.dt
        )

        arrow.isVisible = item.showArrow
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val showArrow: Boolean = true,
        val onClick: () -> Unit
    ) : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
