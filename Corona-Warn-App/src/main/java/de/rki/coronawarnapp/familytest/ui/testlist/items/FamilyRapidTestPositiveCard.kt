package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FamilyRapidTestCardPositivBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPositiveCard.Item
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyRapidTestPositiveCard(parent: ViewGroup) :
    FamilyTestListAdapter.FamilyTestListVH<Item, FamilyRapidTestCardPositivBinding>(
        R.layout.home_card_container_layout,
        parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.familyCoronaTest, holder.bindingAdapterPosition) }
    }

    override val viewBinding = lazy {
        FamilyRapidTestCardPositivBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyRapidTestCardPositivBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        title.text = latestItem!!.familyCoronaTest.personName
        val userDate = latestItem!!.familyCoronaTest.coronaTest.getFormattedRegistrationDate()
        date.text = resources.getString(R.string.family_tests_cards_rapid_date, userDate)
        notificationBadge.isVisible = latestItem!!.familyCoronaTest.hasBadge
        itemView.setOnClickListener { latestItem!!.onClickAction(item) }
    }

    data class Item(
        override val familyCoronaTest: FamilyCoronaTest,
        val onClickAction: (Item) -> Unit,
        val onSwipeItem: (FamilyCoronaTest, Int) -> Unit,
    ) : FamilyTestListItem, HasPayloadDiffer
}
