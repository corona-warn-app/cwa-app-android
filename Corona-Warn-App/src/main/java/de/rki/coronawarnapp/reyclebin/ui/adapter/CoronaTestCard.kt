package de.rki.coronawarnapp.reyclebin.ui.adapter

import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.databinding.RecyclerBinCertificateItemBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.ui.common.addDeletionInfoIfExists
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CoronaTestCard(parent: ViewGroup) :
    RecyclerBinAdapter.ItemVH<CoronaTestCard.Item, RecyclerBinCertificateItemBinding>(
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
        val test = latestItem!!.test

        val (titleRes, iconRes) = when (test) {
            is FamilyCoronaTest -> {
                certificatePersonName.text = test.personName
                R.string.recycle_bin_family_test_item_name to R.drawable.ic_family_test_recycled
            }
            else -> R.string.recycle_bin_test_item_name to R.drawable.ic_personal_test_recycled
        }
        certificateType.setText(titleRes)
        certificateIcon.setImageResource(iconRes)
        certificatePersonName.isVisible = test is FamilyCoronaTest
        certificateInfoLine1.isGone = false

        certificateInfoLine1.setText(
            when (test.type) {
                BaseCoronaTest.Type.PCR -> R.string.test_certificate_pcr_test_type
                else -> R.string.test_certificate_rapid_test_type
            }
        )

        val typeString = when (test.type) {
            BaseCoronaTest.Type.PCR -> R.string.reycle_bin_pcr_test_date
            else -> R.string.reycle_bin_rat_test_date
        }
        certificateInfoLine2.text = when (test) {
            is FamilyCoronaTest -> {
                val time = if (test.type == BaseCoronaTest.Type.PCR) test.registeredAt else test.coronaTest.testTakenAt
                context.getString(
                    typeString,
                    time.toDate().toUIFormat(context)
                )
            }

            is PCRCoronaTest -> context.getString(
                typeString,
                test.registeredAt.toDate().toUIFormat(context)
            )
            is RACoronaTest -> context.getString(
                typeString,
                test.testTakenAt.toDate().toUIFormat(context)
            )
            else -> throw IllegalStateException("Unknown test type ${test.type}")
        }

        addDeletionInfoIfExists(item = test)

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
        val test: BaseCoronaTest,
        val onRemove: (BaseCoronaTest, Int?) -> Unit,
        val onRestore: (BaseCoronaTest) -> Unit
    ) : RecyclerBinItem, HasPayloadDiffer {
        override val stableId = test.hashCode().toLong()
    }

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let {
            it.onRemove(it.test, holder.absoluteAdapterPosition)
        }
    }
}
