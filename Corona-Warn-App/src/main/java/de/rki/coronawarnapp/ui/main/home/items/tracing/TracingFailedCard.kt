package de.rki.coronawarnapp.ui.main.home.items.tracing

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentFailedViewBinding
import de.rki.coronawarnapp.tracing.ui.states.TracingFailed
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingFailedCard.Item

class TracingFailedCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentFailedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentFailedViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentFailedViewBinding.(item: Item) -> Unit = { item ->
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
