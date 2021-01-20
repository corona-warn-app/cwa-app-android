package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsSevendayrvalueLayoutBinding
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString

class SevenDayRValueCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsSevendayrvalueLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsSevendayrvalueLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsSevendayrvalueLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val reproductionNumber = (item.stats as SevenDayRValue).reproductionNumber
        val valueString = formatStatisticalValue(context, reproductionNumber.value, reproductionNumber.decimals)
        primaryValue.text = getLocalizedSpannableString(context, valueString)
        trendView.setTrend(reproductionNumber.trend, reproductionNumber.trendSemantic)
        infoStatistics.setOnClickListener {
            item.onHelpAction.invoke(item.stats)
        }
    }
}
