package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentIncreasedViewBinding
import de.rki.coronawarnapp.tracing.states.IncreasedRisk
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.IncreasedRiskBox.Item

class IncreasedRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_elevated_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentIncreasedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentIncreasedViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        ).also {
            it.root.elevation = resources.getDimension(R.dimen.elevation_strong)
        }
    }

    override val onBindData: TracingContentIncreasedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        state = item.state
    }

    data class Item(
        val state: IncreasedRisk
    ) : RiskStateItem
}
