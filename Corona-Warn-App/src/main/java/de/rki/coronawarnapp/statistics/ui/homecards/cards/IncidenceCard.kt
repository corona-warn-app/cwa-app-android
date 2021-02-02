package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsIncidenceLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
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

            incidenceContainer.contentDescription =
                buildAccessibilityStringForIncidenceCard(item.stats, sevenDayIncidence)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals)
            )

            primaryValue.contentDescription = StringBuilder()
                .append(context.getString(R.string.statistics_explanation_seven_day_incidence_title))
                .append(" ")
                .append(getPrimaryLabel(context))
                .append(" ")
                .append(formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals))
                .append(" ")
                .append(getContentDescriptionForTrends(context, sevenDayIncidence.trend))

            trendArrow.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForIncidenceCard(
        item: StatsItem,
        sevenDayIncidence: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {

        return StringBuilder()
            .append(context.getString(R.string.accessibility_statistics_card_announcement))
            .append(" ")
            .append(context.getString(R.string.statistics_explanation_seven_day_incidence_title))
            .append(" \n ")
            .append(item.getPrimaryLabel(context))
            .append(" ")
            .append(formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals))
            .append(" ")
            .append(context.getString(R.string.statistics_card_incidence_value_description))
            .append(" ")
            .append(getContentDescriptionForTrends(context, sevenDayIncidence.trend))
            .append(" \n ")
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
