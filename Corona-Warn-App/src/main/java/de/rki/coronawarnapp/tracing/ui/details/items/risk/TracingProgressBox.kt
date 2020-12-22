package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentProgressViewBinding
import de.rki.coronawarnapp.tracing.states.TracingInProgress
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingProgressBox.Item

class TracingProgressBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_elevated_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentProgressViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentProgressViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        ).also {
            it.root.elevation = resources.getDimension(R.dimen.elevation_strong)
        }
    }

    override val onBindData: TracingContentProgressViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        itemView.setBackgroundColor(item.state.getContainerColor(context))
        state = item.state
    }

    data class Item(
        val state: TracingInProgress
    ) : RiskStateItem
}
