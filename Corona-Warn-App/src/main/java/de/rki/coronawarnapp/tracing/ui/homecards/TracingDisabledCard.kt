package de.rki.coronawarnapp.tracing.ui.homecards

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentDisabledViewBinding
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.ui.homecards.TracingDisabledCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TracingDisabledCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentDisabledViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentDisabledViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentDisabledViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        state = item.state
        itemView.setOnClickListener { item.onCardClick(item) }
        enableTracingAction.setOnClickListener { item.onEnableTracingClick(item) }
    }

    data class Item(
        val state: TracingDisabled,
        val onCardClick: (Item) -> Unit,
        val onEnableTracingClick: (Item) -> Unit
    ) : TracingStateItem
}
