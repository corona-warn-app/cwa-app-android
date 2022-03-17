package de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.DccTicketingTestCardBinding
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DccTicketingTestCard(parent: ViewGroup) :
    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<DccTicketingTestCard.Item, DccTicketingTestCardBinding>(
        layoutRes = R.layout.dcc_ticketing_test_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<DccTicketingTestCardBinding> = lazy {
        DccTicketingTestCardBinding.bind(itemView)
    }

    override val onBindData: DccTicketingTestCardBinding.(item: Item, payloads: List<Any>) -> Unit = { item, payloads ->

        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val certificate = curItem.certificate
        root.setOnClickListener { curItem.onClick() }
        certificateDate.text = context.getString(
            R.string.test_certificate_sampled_on,
            certificate.sampleCollectedAt?.toUserTimeZone()?.toShortDayFormat() ?: certificate.rawCertificate.test.sc
        )

        arrow.isVisible = item.showArrow

        when {
            // PCR Test
            certificate.isPCRTestCertificate -> R.string.test_certificate_pcr_test_type
            // RAT Test
            else -> R.string.test_certificate_rapid_test_type
        }.also { testCertificateType.setText(it) }
    }

    data class Item(
        val certificate: TestCertificate,
        val showArrow: Boolean = true,
        val onClick: () -> Unit
    ) : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
