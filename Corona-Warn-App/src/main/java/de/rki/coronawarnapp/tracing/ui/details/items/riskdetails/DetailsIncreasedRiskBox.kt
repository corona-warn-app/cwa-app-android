package de.rki.coronawarnapp.tracing.ui.details.items.riskdetails

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemRiskdetailsIncreasedViewBinding
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox.Item

class DetailsIncreasedRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsItemRiskdetailsIncreasedViewBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsItemRiskdetailsIncreasedViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemRiskdetailsIncreasedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        info = item
    }

    data class Item(
        val riskState: RiskState,
        val lastEncounterDaysAgo: Int
    ) : RiskDetailsStateItem {

        fun getRiskDetailsRiskLevelBody(c: Context): String = c.resources.getQuantityString(
            R.plurals.risk_details_information_body_increased_risk,
            lastEncounterDaysAgo,
            lastEncounterDaysAgo
        )
    }
}
