package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsIncidenceLayoutBinding
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class IncidenceCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsIncidenceLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsIncidenceLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsIncidenceLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        iconInfo.setOnClickListener {
            item.onHelpAction.invoke(item.stats)
        }

        val sevenDayIncidence = (item.stats as IncidenceStats).sevenDayIncidence

        valueLabel.text = item.stats.getPrimaryLabel(context)

        val formattedValue = formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals)
        valuePrimary.text = getLocalizedSpannableString(context, formattedValue)

        trendArrowView.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)
    }
}
