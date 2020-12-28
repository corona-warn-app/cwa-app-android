package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentDisabledViewBinding
import de.rki.coronawarnapp.tracing.states.TracingDisabled
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingDisabledBox.Item
import de.rki.coronawarnapp.util.ui.setGone

class TracingDisabledBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_elevated_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentDisabledViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentDisabledViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        ).also {
            it.root.elevation = resources.getDimension(R.dimen.elevation_strong)
        }
    }

    override val onBindData: TracingContentDisabledViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        state = item.state
        enableTracingAction.setGone(item.state.isInDetailsMode)
        detailsIcon.setGone(item.state.isInDetailsMode)
    }

    data class Item(
        val state: TracingDisabled
    ) : RiskStateItem
}
