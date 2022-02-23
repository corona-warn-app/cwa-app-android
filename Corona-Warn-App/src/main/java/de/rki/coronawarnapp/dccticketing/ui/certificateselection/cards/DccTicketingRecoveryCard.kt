package de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.DccTicketingRecoveryCardBinding
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DccTicketingRecoveryCard(parent: ViewGroup) :
    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<
        DccTicketingRecoveryCard.Item,
        DccTicketingRecoveryCardBinding>(
        layoutRes = R.layout.dcc_ticketing_recovery_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<DccTicketingRecoveryCardBinding> = lazy {
        DccTicketingRecoveryCardBinding.bind(itemView)
    }

    override val onBindData: DccTicketingRecoveryCardBinding.(item: Item, payloads: List<Any>) -> Unit =
        { item, payloads ->

            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
            val certificate = curItem.certificate
            root.setOnClickListener { curItem.onClick() }

            certificateDate.text = context.getString(
                R.string.recovery_certificate_valid_until,
                certificate.validUntil?.toShortDayFormat() ?: certificate.rawCertificate.recovery.du
            )

            arrow.isVisible = item.showArrow
        }

    data class Item(
        val certificate: RecoveryCertificate,
        val showArrow: Boolean = true,
        val onClick: () -> Unit
    ) : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
