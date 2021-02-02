package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsInfectionsLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends

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

        infoStatistics.setOnClickListener {
            item.onHelpAction.invoke(item.stats)
        }

        with(item.stats as InfectionStats) {

            infectionsContainer.contentDescription =
                buildAccessibilityStringForInfectionsCard(item.stats, newInfections, sevenDayAverage, total)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = formatStatisticalValue(context, newInfections.value, newInfections.decimals)
            primaryValue.contentDescription = StringBuilder()
                .append(getPrimaryLabel(context))
                .append(" ")
                .append(formatStatisticalValue(context, newInfections.value, newInfections.decimals))
                .append(" ")
                .append(context.getString(R.string.statistics_card_infections_title))

            secondaryValue.text = formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals)
            secondaryValue.contentDescription = StringBuilder()
                .append(context.getString(R.string.statistics_card_infections_secondary_label))
                .append(" ")
                .append(formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals))
                .append(" ")
                .append(context.getString(R.string.statistics_card_infections_title))
                .append(" ")
                .append(getContentDescriptionForTrends(context, sevenDayAverage.trend))

            tertiaryValue.text = formatStatisticalValue(context, total.value, total.decimals)
            tertiaryValue.contentDescription = StringBuilder()
                .append(context.getString(R.string.statistics_card_infections_tertiary_label))
                .append(" ")
                .append(formatStatisticalValue(context, total.value, total.decimals))
                .append(" ")
                .append(context.getString(R.string.statistics_card_infections_title))

            trendArrow.setTrend(sevenDayAverage.trend, sevenDayAverage.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForInfectionsCard(
        item: StatsItem,
        newInfections: KeyFigureCardOuterClass.KeyFigure,
        sevenDayAverage: KeyFigureCardOuterClass.KeyFigure,
        total: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {

        return StringBuilder()
            .append(context.getString(R.string.accessibility_statistics_card_announcement))
            .append(" ")
            .append(context.getString(R.string.statistics_card_infections_title))
            .append(" \n ")
            .append(item.getPrimaryLabel(context))
            .append(" ")
            .append(formatStatisticalValue(context, newInfections.value, newInfections.decimals))
            .append(" \n ")
            .append(context.getString(R.string.statistics_card_infections_secondary_label))
            .append(" ")
            .append(formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals))
            .append(" ")
            .append(getContentDescriptionForTrends(context, sevenDayAverage.trend))
            .append(" \n ")
            .append(context.getString(R.string.statistics_card_infections_tertiary_label))
            .append(" ")
            .append(formatStatisticalValue(context, total.value, total.decimals))
            .append(" \n ")
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
