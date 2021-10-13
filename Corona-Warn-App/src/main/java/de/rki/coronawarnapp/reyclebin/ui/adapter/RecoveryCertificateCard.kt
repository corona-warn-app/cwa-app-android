package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class RecoveryCertificateCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<RecoveryCertificateCard.Item, RecyclerBinCertificateItemBinding>(
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

        certificateInfoLine1.isGone = true
        certificateInfoLine2.text = context.getString(
            R.string.recovery_certificate_valid_until,
            certificate.validUntil.toShortDayFormat()
        )
        certificatePersonName.text = certificate.fullNameFormatted
        certificateType.setText(R.string.recovery_certificate_name)
    }

    data class Item(
        val certificate: RecoveryCertificate,
        val onClick: () -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.containerId.hashCode().toLong()
    }
}
