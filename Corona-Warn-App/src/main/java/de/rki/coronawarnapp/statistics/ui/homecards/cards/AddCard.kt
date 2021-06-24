package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.graphics.ColorFilter
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsAddLayoutBinding
import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.AppliedVaccinationRatesStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter

class AddCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsAddLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsAddLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsAddLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        with(item.stats as AddStatsItem) {
            if (isEnabled) {
                warningText.isGone = true
                plusImage.clearColorFilter()
                titleText.setTextColor(ContextCompat.getColor(context, R.color.colorStatisticsPrimaryValue))
            } else {
                warningText.isGone = false
                plusImage.setColorFilter(ContextCompat.getColor(context, R.color.colorStatisticsValueLabel))
                titleText.setTextColor(ContextCompat.getColor(context, R.color.colorStatisticsValueLabel))
            }
        }
    }
}
