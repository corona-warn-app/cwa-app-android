package de.rki.coronawarnapp.tracing.ui.details.items.survey

import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsAccessSurveyCardBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

class UserSurveyBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_access_survey_card
) : TracingDetailsAdapter.DetailsItemVH<UserSurveyBox.Item, TracingDetailsAccessSurveyCardBinding>(
    containerLayout,
    parent
) {

    override val viewBinding: Lazy<TracingDetailsAccessSurveyCardBinding> = lazy {
        TracingDetailsAccessSurveyCardBinding.bind(itemView)
    }

    override val onBindData: TracingDetailsAccessSurveyCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ ->
        tracingDetailsSurveyCardButton.setOnClickListener {
            Toast.makeText(
                context,
                "Still WIP ;)",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    class Item : DetailsItem {
        override val stableId: Long
            get() = Item::class.java.name.hashCode().toLong()
    }
}
