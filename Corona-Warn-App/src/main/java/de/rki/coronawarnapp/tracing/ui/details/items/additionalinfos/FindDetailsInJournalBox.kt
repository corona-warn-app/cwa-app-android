package de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsFindDetailsInJournalBinding
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.FindDetailsInJournalBox.Item

class FindDetailsInJournalBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_find_details_in_journal
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsFindDetailsInJournalBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsFindDetailsInJournalBinding.bind(itemView)
    }

    override val onBindData: TracingDetailsFindDetailsInJournalBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        riskDetailsFindDetailsInJournal.text = curItem.getText(context)
    }

    data class Item(
        val riskState: RiskState
    ) : AdditionalInformationItem {
        fun getText(context: Context): String = when (riskState == RiskState.INCREASED_RISK) {
            true -> R.string.risk_details_find_details_in_journal_increased_risk
            false -> R.string.risk_details_find_details_in_journal
        }.let { context.getString(it) }
    }
}
