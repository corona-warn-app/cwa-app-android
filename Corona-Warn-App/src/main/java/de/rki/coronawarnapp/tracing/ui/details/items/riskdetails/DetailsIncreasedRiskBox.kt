package de.rki.coronawarnapp.tracing.ui.details.items.riskdetails

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemRiskdetailsIncreasedViewBinding
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox.Item
import java.time.LocalDate

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
        val lastEncounteredAt: LocalDate
    ) : RiskDetailsStateItem {

        fun getRiskDetailsRiskLevelBody(context: Context): String {
            return context.getString(R.string.risk_details_information_body_increased_risk_date)
        }
    }
}
