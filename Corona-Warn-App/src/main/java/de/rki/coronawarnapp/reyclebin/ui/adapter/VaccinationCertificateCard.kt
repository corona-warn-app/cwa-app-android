package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class VaccinationCertificateCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<VaccinationCertificateCard.Item, RecyclerBinCertificateItemBinding>(
        layoutRes = R.layout.recycler_bin_certificate_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<RecyclerBinCertificateItemBinding> = lazy {
        RecyclerBinCertificateItemBinding.bind(itemView)
    }
    override val onBindData: RecyclerBinCertificateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }
        certificateInfoLine1.text = context.getString(
            R.string.vaccination_certificate_doses,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
        certificateInfoLine2.text = context.getString(
            R.string.vaccination_certificate_vaccinated_on,
            certificate.vaccinatedOn.toShortDayFormat()
        )
        certificatePersonName.text = certificate.fullNameFormatted
        certificateType.setText(R.string.vaccination_certificate_name)
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val onClick: () -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
