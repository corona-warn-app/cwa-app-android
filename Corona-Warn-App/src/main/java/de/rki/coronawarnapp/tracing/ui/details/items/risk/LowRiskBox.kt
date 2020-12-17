package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentLowViewBinding
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.LowRiskBox.Item
import de.rki.coronawarnapp.util.ui.setGone

class LowRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_elevated_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentLowViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentLowViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        ).also {
            it.root.elevation = resources.getDimension(R.dimen.elevation_strong)
        }
    }

    override val onBindData: TracingContentLowViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        state = item.state
        updateAction.setGone(item.state.isInDetailsMode)
        detailsIcon.setGone(item.state.isInDetailsMode)
    }

    data class Item(
        val state: LowRisk
    ) : RiskStateItem
}
