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
    ) -> Unit = { item, _ ->

        infoStatistics.setOnClickListener {
            item.onHelpAction.invoke(item.stats)
        }

        with(item.stats as IncidenceStats) {
            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals)
            )
            trendArrow.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)
        }
    }
}
