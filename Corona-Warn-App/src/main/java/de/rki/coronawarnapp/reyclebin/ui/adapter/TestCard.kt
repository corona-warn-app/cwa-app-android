package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTest
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import java.lang.IllegalStateException

class TestCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<TestCard.Item, RecyclerBinCertificateItemBinding>(
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

        latestItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val test = latestItem!!.test

        certificateType.setText(R.string.recycle_bin_test_item_name)
        certificateIcon.setImageResource(R.drawable.ic_test_filled_white)
        certificatePersonName.isGone = true
        certificateInfoLine1.isGone = false

        certificateInfoLine1.setText(
            when (test.coronaTest) {
                is PCRCoronaTest -> R.string.test_certificate_pcr_test_type
                else -> R.string.test_certificate_rapid_test_type
            }
        )
        certificateInfoLine2.text = when (test.coronaTest) {
            is PCRCoronaTest -> context.getString(
                R.string.test_result_card_registered_at_text,
                test.coronaTest.registeredAt.toDate().toUIFormat(context)
            )
            is RACoronaTest -> context.getString(
                R.string.ag_homescreen_card_rapid_body_result_date,
                test.coronaTest.testTakenAt.toDate().toUIFormat(context)
            )
            else -> throw IllegalStateException("Unknown test type ${test.coronaTest}")
        }

        root.setOnClickListener { item.onRestore(item.test) }

        menuAction.setupMenu(R.menu.menu_recycler_bin_list_item) {
            when (it.itemId) {
                R.id.menu_remove_permanently -> item.onRemove(item.test, null).let { true }
                R.id.menu_restore -> item.onRestore(item.test).let { true }
                else -> false
            }
        }
    }

    data class Item(
        val test: RecycledCoronaTest,
        val onRemove: (RecycledCoronaTest, Int?) -> Unit,
        val onRestore: (RecycledCoronaTest) -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = test.coronaTest.hashCode().toLong()
    }

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let {
            it.onRemove(it.test, holder.absoluteAdapterPosition)
        }
    }
}
