package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentFailedViewBinding
import de.rki.coronawarnapp.tracing.states.TracingFailed
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingFailedBox.Item
import de.rki.coronawarnapp.util.ui.setGone

class TracingFailedBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentFailedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentFailedViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.box_container), true)
    }

    override val onBindData: TracingContentFailedViewBinding.(item: Item) -> Unit = { item ->
        state = item.state
        riskCardButtonUpdate.setGone(item.state.isInDetailsMode)
        riskCardHeaderIcon.setGone(item.state.isInDetailsMode)
    }

    data class Item(
        val state: TracingFailed
    ) : RiskStateItem
}
