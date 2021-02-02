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
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithWhiteSpace

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
                .appendWithWhiteSpace(getPrimaryLabel(context))
                .appendWithWhiteSpace(formatStatisticalValue(context, newInfections.value, newInfections.decimals))
                .append(context.getString(R.string.statistics_card_infections_title))

            secondaryValue.text = formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals)
            secondaryValue.contentDescription = StringBuilder()
                .appendWithWhiteSpace(context.getString(R.string.statistics_card_infections_secondary_label))
                .appendWithWhiteSpace(formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals))
                .appendWithWhiteSpace(context.getString(R.string.statistics_card_infections_title))
                .append(getContentDescriptionForTrends(context, sevenDayAverage.trend))

            tertiaryValue.text = formatStatisticalValue(context, total.value, total.decimals)
            tertiaryValue.contentDescription = StringBuilder()
                .appendWithWhiteSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
                .appendWithWhiteSpace(formatStatisticalValue(context, total.value, total.decimals))
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
            .appendWithWhiteSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_card_infections_title))
            .appendWithWhiteSpace(item.getPrimaryLabel(context))
            .appendWithLineBreak(formatStatisticalValue(context, newInfections.value, newInfections.decimals))
            .appendWithWhiteSpace(context.getString(R.string.statistics_card_infections_secondary_label))
            .appendWithWhiteSpace(formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals))
            .appendWithLineBreak(getContentDescriptionForTrends(context, sevenDayAverage.trend))
            .appendWithWhiteSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
            .appendWithLineBreak(formatStatisticalValue(context, total.value, total.decimals))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
