package de.rki.coronawarnapp.ui.tracing.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentProgressViewBinding
import de.rki.coronawarnapp.tracing.ui.states.TracingInProgress
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsAdapter
import de.rki.coronawarnapp.ui.tracing.details.items.risk.TracingProgressBox.Item

class TracingProgressBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentProgressViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentProgressViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.box_container), true)
    }

    override val onBindData: TracingContentProgressViewBinding.(item: Item) -> Unit = { item ->
        state = item.state
        itemView.backgroundTintList = item.state.getRiskInfoContainerBackgroundTint(context)
    }

    data class Item(
        val state: TracingInProgress
    ) : RiskStateItem
}
