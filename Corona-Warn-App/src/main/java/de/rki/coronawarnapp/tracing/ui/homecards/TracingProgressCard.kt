package de.rki.coronawarnapp.tracing.ui.homecards

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentProgressViewBinding
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.ui.homecards.TracingProgressCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TracingProgressCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentProgressViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentProgressViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentProgressViewBinding.(item: Item) -> Unit = { item ->
        itemView.backgroundTintList = item.state.getRiskInfoContainerBackgroundTint(context)
        state = item.state

        itemView.setOnClickListener { item.onCardClick(item) }
    }

    data class Item(
        val state: TracingInProgress,
        val onCardClick: (Item) -> Unit,
    ) : TracingStateItem
}
