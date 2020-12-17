package de.rki.coronawarnapp.tracing.ui.details.items.riskdetails

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemRiskdetailsLowViewBinding
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsLowRiskBox.Item
import de.rki.coronawarnapp.util.convertToHyperlink

class DetailsLowRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsItemRiskdetailsLowViewBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsItemRiskdetailsLowViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemRiskdetailsLowViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        info = item
        riskDetailsInformationLowriskBodyUrl.convertToHyperlink(
            context.getString(R.string.risk_details_explanation_faq_link)
        )

        riskDetailsInformationLowriskBodyUrl.movementMethod = LinkMovementMethod.getInstance()
    }

    data class Item(
        val riskState: RiskState,
        val matchedKeyCount: Int
    ) : RiskDetailsStateItem {

        fun getRiskDetailsRiskLevelBody(c: Context): String {
            return c.getString(
                if (matchedKeyCount > 0) R.string.risk_details_information_body_low_risk_with_encounter
                else R.string.risk_details_information_body_low_risk
            )
        }
    }
}
