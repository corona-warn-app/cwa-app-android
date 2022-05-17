package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FamilyPcrTestCardFinalBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestNegativeCard.Item
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyPcrTestNegativeCard(parent: ViewGroup) :
    FamilyTestListAdapter.FamilyTestListVH<Item, FamilyPcrTestCardFinalBinding>(
        R.layout.home_card_container_layout,
        parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.familyCoronaTest, holder.bindingAdapterPosition) }
    }

    override val viewBinding = lazy {
        FamilyPcrTestCardFinalBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyPcrTestCardFinalBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        latestItem?.let {
            title.text = it.familyCoronaTest.personName
            val userDate = it.familyCoronaTest.coronaTest.getFormattedRegistrationDate()
            date.text = resources.getString(R.string.family_tests_cards_pcr_date, userDate)
            notificationBadge.isVisible = it.familyCoronaTest.hasBadge
            itemView.setOnClickListener { _ ->
                it.onClickAction(item)
            }
        }

        status.setTextColor(parent.resources.getColor(R.color.colorTextSemanticGreen, null))
        status.setText(R.string.ag_homescreen_card_status_negative)
        icon.setImageResource(R.drawable.ic_test_result_illustration_negative_card)
    }

    data class Item(
        override val familyCoronaTest: FamilyCoronaTest,
        val onClickAction: (Item) -> Unit,
        val onSwipeItem: (FamilyCoronaTest, Int) -> Unit,
    ) : FamilyTestListItem, HasPayloadDiffer
}
