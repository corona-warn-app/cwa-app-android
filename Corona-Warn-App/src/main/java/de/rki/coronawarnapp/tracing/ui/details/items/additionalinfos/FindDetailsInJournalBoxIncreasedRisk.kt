package de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsFindDetailsInJournalIncreasedRiskBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.FindDetailsInJournalBoxIncreasedRisk.Item

class FindDetailsInJournalBoxIncreasedRisk(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_find_details_in_journal_increased_risk
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsFindDetailsInJournalIncreasedRiskBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsFindDetailsInJournalIncreasedRiskBinding.bind(itemView)
    }

    override val onBindData: TracingDetailsFindDetailsInJournalIncreasedRiskBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    object Item : AdditionalInformationItem
}
