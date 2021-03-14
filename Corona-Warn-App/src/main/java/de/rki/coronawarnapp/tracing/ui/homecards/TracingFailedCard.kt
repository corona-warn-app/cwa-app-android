package de.rki.coronawarnapp.tracing.ui.homecards

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentFailedViewBinding
import de.rki.coronawarnapp.tracing.states.TracingFailed
import de.rki.coronawarnapp.tracing.ui.homecards.TracingFailedCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TracingFailedCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentFailedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentFailedViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentFailedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        state = item.state

        itemView.setOnClickListener { item.onCardClick(item) }
        riskCardButtonUpdate.setOnClickListener { item.onRetryClick(item) }
    }

    data class Item(
        val state: TracingFailed,
        val onCardClick: (Item) -> Unit,
        val onRetryClick: (Item) -> Unit
    ) : TracingStateItem
}
