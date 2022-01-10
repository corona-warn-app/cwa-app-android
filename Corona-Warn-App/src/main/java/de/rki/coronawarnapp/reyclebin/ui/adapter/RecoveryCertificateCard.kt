package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.reyclebin.ui.common.addDeletionInfoIfExists
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RecoveryCertificateCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<RecoveryCertificateCard.Item, RecyclerBinCertificateItemBinding>(
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
        certificateInfoLine1.isGone = true
        certificateInfoLine2.text = context.getString(
            R.string.recovery_certificate_valid_until,
            certificate.validUntil.toShortDayFormat()
        )
        certificatePersonName.text = certificate.fullName
        certificateType.setText(R.string.recovery_certificate_name)

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
        val certificate: RecoveryCertificate,
        val onRemove: (RecoveryCertificate, Int?) -> Unit,
        val onRestore: (RecoveryCertificate) -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override val stableId: Long = certificate.containerId.hashCode().toLong()
    }

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let {
            it.onRemove(it.certificate, holder.absoluteAdapterPosition)
        }
    }
}
