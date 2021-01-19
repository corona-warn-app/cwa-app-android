package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsKeysubmissionsLayoutBinding
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class KeySubmissionsCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsKeysubmissionsLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsKeysubmissionsLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsKeysubmissionsLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        with(item.stats as KeySubmissionsStats) {
            viewBinding.value.newInfectionsLabel = getPrimaryLabel(context)

            viewBinding.value.newInfections =
                formatStatisticalValue(context, keySubmissions.value, keySubmissions.decimals)
            viewBinding.value.sevenDayAverage =
                formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals)
            viewBinding.value.total =
                formatStatisticalValue(context, total.value, total.decimals)

            viewBinding.value.trendArrowView.setTrend(sevenDayAverage.trend, sevenDayAverage.trendSemantic)
        }
    }
}
