package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsInfectionsLayoutBinding
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class InfectionsCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsInfectionsLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsInfectionsLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsInfectionsLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        with(item.stats as InfectionStats) {
            viewBinding.value.newInfectionsLabel = getPrimaryLabel(context)

            viewBinding.value.newInfections =
                formatStatisticalValue(context, newInfections.value, newInfections.decimals)
            viewBinding.value.sevenDayAverage =
                formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals)
            viewBinding.value.total =
                formatStatisticalValue(context, total.value, total.decimals)

            viewBinding.value.trendArrowView.setTrend(sevenDayAverage.trend, sevenDayAverage.trendSemantic)
        }
    }
}
