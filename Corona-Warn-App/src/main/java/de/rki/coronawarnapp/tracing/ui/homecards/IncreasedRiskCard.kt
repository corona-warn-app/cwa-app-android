package de.rki.coronawarnapp.tracing.ui.homecards

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentIncreasedViewBinding
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.ui.homecards.IncreasedRiskCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class IncreasedRiskCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentIncreasedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentIncreasedViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentIncreasedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item.state.apply {
            rowContact.isGone = getRiskContactBody(context) == null
            rowContact.contentDescription = getRiskContactBodyDescription(context)
            rowContact.setText(getRiskContactBody(context))
            rowContactLast.isGone = getRiskContactLast(context) == null
            rowContactLast.setText(getRiskContactLast(context))
            rowTimeFetched.setText(getTimeFetched(context))
        }
        itemView.setOnClickListener { item.onCardClick(item) }
        updateAction.setOnClickListener { item.onUpdateClick(item) }
    }

    data class Item(
        val state: IncreasedRisk,
        val onCardClick: (Item) -> Unit,
        val onUpdateClick: (Item) -> Unit
    ) : TracingStateItem
}
