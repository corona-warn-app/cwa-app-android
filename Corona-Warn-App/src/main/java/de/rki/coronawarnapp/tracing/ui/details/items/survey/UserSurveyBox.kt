package de.rki.coronawarnapp.tracing.ui.details.items.survey

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsAccessSurveyCardBinding
import de.rki.coronawarnapp.datadonation.survey.Surveys
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

class UserSurveyBox(
    parent: ViewGroup,
    private val onItemClickListener: (item: DetailsItem) -> Unit,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : TracingDetailsAdapter.DetailsItemVH<UserSurveyBox.Item, TracingDetailsAccessSurveyCardBinding>(
    containerLayout,
    parent
) {

    override val viewBinding: Lazy<TracingDetailsAccessSurveyCardBinding> = lazy {
        TracingDetailsAccessSurveyCardBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: TracingDetailsAccessSurveyCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        itemView.setOnClickListener { onItemClickListener(item) }
        tracingDetailsSurveyCardButton.setOnClickListener { onItemClickListener(item) }
    }

    data class Item(
        val type: Surveys.Type
    ) : DetailsItem {
        override val stableId: Long
            get() = Item::class.java.name.hashCode().toLong()
    }
}
