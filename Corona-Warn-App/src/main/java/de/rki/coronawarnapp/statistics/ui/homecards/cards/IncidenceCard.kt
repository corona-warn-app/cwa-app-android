package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsIncidenceLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
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

        viewBinding.value.iconInfo.setOnClickListener {
            item.onHelpAction.invoke(item.stats)
        }

        val stats = item.stats as IncidenceStats

        val primaryFigure =
            stats.keyFigures.firstOrNull { it.rank == KeyFigureCardOuterClass.KeyFigure.Rank.PRIMARY }

        if (primaryFigure != null) {
            with(viewBinding.value) {
                valueLabel.text = stats.getPrimaryLabel(context)
                val formattedValue = formatStatisticalValue(context, primaryFigure.value, primaryFigure.decimals)
                valuePrimary.text = getLocalizedSpannableString(context, formattedValue)
                trendArrowView.setTrend(primaryFigure.trend, primaryFigure.trendSemantic)
            }
        }
    }
}
