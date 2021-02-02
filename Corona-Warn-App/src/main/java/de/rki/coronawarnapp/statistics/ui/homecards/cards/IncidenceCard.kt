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

            primaryValue.contentDescription =
                context.getString(R.string.statistics_explanation_seven_day_incidence_title) + " " +
                    getPrimaryLabel(context) + " " +
                    formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals) + " " +
                    getContentDescriptionForTrends(context, sevenDayIncidence.trend)

            trendArrow.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForIncidenceCard(
        item: StatsItem,
        sevenDayIncidence: KeyFigureCardOuterClass.KeyFigure
    ): String {
        return context.getString(R.string.accessibility_statistics_card_announcement) + " " +
            context.getString(R.string.statistics_explanation_seven_day_incidence_title) + " \n " +
            item.getPrimaryLabel(context) + " " +
            formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals) + " " +
            context.getString(R.string.statistics_card_incidence_value_description) + " " +
            getContentDescriptionForTrends(context, sevenDayIncidence.trend) + " \n " +
            context.getString(R.string.accessibility_statistics_card_navigation_information)
    }
}
