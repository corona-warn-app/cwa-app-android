package de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemAdditionalInformationViewBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.AdditionalInfoLowRiskBox.Item

class AdditionalInfoLowRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsItemAdditionalInformationViewBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsItemAdditionalInformationViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemAdditionalInformationViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ -> }

    object Item : AdditionalInformationItem
}
