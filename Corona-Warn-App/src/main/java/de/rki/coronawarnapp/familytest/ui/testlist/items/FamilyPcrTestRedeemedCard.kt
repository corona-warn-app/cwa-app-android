package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FamilyPcrTestCardRedeemedBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard.Item
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyPcrTestRedeemedCard(parent: ViewGroup) :
    FamilyTestListAdapter.FamilyTestListVH<Item, FamilyPcrTestCardRedeemedBinding> (
        R.layout.home_card_container_layout,
        parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.familyCoronaTest, holder.bindingAdapterPosition) }
    }

    override val viewBinding = lazy {
        FamilyPcrTestCardRedeemedBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyPcrTestCardRedeemedBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        title.text = latestItem!!.familyCoronaTest.personName
        deleteTestAction.setOnClickListener { latestItem!!.onDeleteTest(item) }
    }

    data class Item(
        override val familyCoronaTest: FamilyCoronaTest,
        val onSwipeItem: (FamilyCoronaTest, Int) -> Unit,
        val onDeleteTest: (Item) -> Unit
    ) : FamilyTestListItem, HasPayloadDiffer
}
