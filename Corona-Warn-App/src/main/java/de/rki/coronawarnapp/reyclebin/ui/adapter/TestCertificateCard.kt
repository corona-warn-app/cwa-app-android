package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.reyclebin.ui.common.addDeletionInfoIfExists
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TestCertificateCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<TestCertificateCard.Item, RecyclerBinCertificateItemBinding>(
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
        certificateInfoLine2.text = context.getString(
            R.string.test_certificate_sampled_on,
            certificate.sampleCollectedAt?.toUserTimeZone()?.toShortDayFormat() ?: certificate.rawCertificate.test.sc
        )

        when (certificate.rawCertificate.test.testType) {
            // PCR Test
            "LP6464-4" -> R.string.test_certificate_pcr_test_type
            // RAT Test
            else -> R.string.test_certificate_rapid_test_type
        }.also { certificateInfoLine1.setText(it) }

        certificatePersonName.text = certificate.fullName
        certificateType.setText(R.string.test_certificate_name)

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
        val certificate: TestCertificate,
        val onRemove: (TestCertificate, Int?) -> Unit,
        val onRestore: (TestCertificate) -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override val stableId = certificate.containerId.hashCode().toLong()
    }

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let {
            it.onRemove(it.certificate, holder.absoluteAdapterPosition)
        }
    }
}
