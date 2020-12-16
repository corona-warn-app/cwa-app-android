package de.rki.coronawarnapp.ui.tracing.details.items.riskdetails

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemRiskdetailsFailedViewBinding
import de.rki.coronawarnapp.ui.tracing.details.TracingDetailsAdapter
import de.rki.coronawarnapp.ui.tracing.details.items.riskdetails.DetailsFailedCalculationBox.Item

class DetailsFailedCalculationBox(
    parent: ViewGroup, @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsItemRiskdetailsFailedViewBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsItemRiskdetailsFailedViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemRiskdetailsFailedViewBinding.(item: Item) -> Unit = { item ->

    }

    object Item : RiskDetailsStateItem
}
