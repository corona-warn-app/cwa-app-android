package de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsFindDetailsInJournalBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter

class FindDetailsInJournalBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_find_details_in_journal
) : TracingDetailsAdapter.DetailsItemVH<FindDetailsInJournalBox.Item, TracingDetailsFindDetailsInJournalBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsFindDetailsInJournalBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsFindDetailsInJournalBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    object Item : AdditionalInformationItem
}
