package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TestCertificateCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<TestCertificateCard.Item, RecyclerBinCertificateItemBinding>(
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
        certificateInfoLine2.text = context.getString(
            R.string.test_certificate_sampled_on,
            certificate.sampleCollectedAt.toUserTimeZone().toShortDayFormat()
        )

        when (certificate.rawCertificate.test.testType) {
            // PCR Test
            "LP6464-4" -> R.string.test_certificate_pcr_test_type
            // RAT Test
            else -> R.string.test_certificate_rapid_test_type
        }.also { certificateInfoLine1.setText(it) }

        certificatePersonName.text = certificate.fullNameFormatted
        certificateType.setText(R.string.recovery_certificate_name)
    }

    data class Item(
        val certificate: TestCertificate,
        val onClick: () -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = certificate.containerId.hashCode().toLong()
    }
}
