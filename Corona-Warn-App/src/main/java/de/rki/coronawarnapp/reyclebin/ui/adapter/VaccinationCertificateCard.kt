package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.reyclebin.ui.common.addDeletionInfoIfExists
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class VaccinationCertificateCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<VaccinationCertificateCard.Item, RecyclerBinCertificateItemBinding>(
        layoutRes = R.layout.recycler_bin_certificate_item,
        parent = parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override val viewBinding: Lazy<RecyclerBinCertificateItemBinding> = lazy {
        RecyclerBinCertificateItemBinding.bind(itemView)
    }
    override val onBindData: RecyclerBinCertificateItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val certificate = latestItem!!.certificate

        certificateIcon.setImageResource(R.drawable.ic_certificates_filled_white)
        certificatePersonName.isGone = false
        certificateInfoLine1.isGone = false
        certificateInfoLine1.text = context.getString(
            R.string.vaccination_certificate_doses,
            certificate.doseNumber,
            certificate.totalSeriesOfDoses
        )
        certificateInfoLine2.text = context.getString(
            R.string.vaccination_certificate_vaccinated_on,
            certificate.vaccinatedOn?.toShortDayFormat()
        )
        certificatePersonName.text = certificate.fullName
        certificateType.setText(R.string.vaccination_certificate_name)

        addDeletionInfoIfExists(item = certificate)

        root.setOnClickListener { item.onRestore(item.certificate) }

        menuAction.setupMenu(R.menu.menu_recycler_bin_list_item) {
            when (it.itemId) {
                R.id.menu_remove_permanently -> item.onRemove(item.certificate, null).let { true }
                R.id.menu_restore -> item.onRestore(item.certificate).let { true }
                else -> false
            }
        }
    }

    data class Item(
        val certificate: VaccinationCertificate,
        val onRemove: (VaccinationCertificate, Int?) -> Unit,
        val onRestore: (VaccinationCertificate) -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override val stableId = certificate.containerId.hashCode().toLong()
    }

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let {
            it.onRemove(it.certificate, holder.absoluteAdapterPosition)
        }
    }
}
