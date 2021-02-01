package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import androidx.core.view.contains
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsSevendayrvalueLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

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
    ) -> Unit = { item, _ ->

        infoStatistics.setOnClickListener {
            item.onHelpAction.invoke(item.stats)
        }

        with(item.stats as SevenDayRValue) {

            sevenDayRValueContainer.contentDescription =
                buildAccessibilityStringForSevenDayRValueCard(item.stats, reproductionNumber)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, reproductionNumber.value, reproductionNumber.decimals)
            )

            primaryValue.contentDescription =
                context.getString(R.string.statistics_title_reproduction) + " " +
                    getPrimaryLabel(context) + " " +
                    formatStatisticalValue(context, reproductionNumber.value, reproductionNumber.decimals) +
                    getContentDescriptionForTrends(context, reproductionNumber.trend)

            trendArrow.setTrend(reproductionNumber.trend, reproductionNumber.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForSevenDayRValueCard(
        item: StatsItem,
        reproductionNumber: KeyFigureCardOuterClass.KeyFigure
    ): String {
        return context.getString(R.string.accessibility_statistics_card_announcement) +
            context.getString(R.string.statistics_title_reproduction) + "\n" +
            item.getPrimaryLabel(context) + " " +
            formatStatisticalValue(context, reproductionNumber.value, reproductionNumber.decimals) +
            context.getString(R.string.statistics_card_incidence_value_description) + " " +
            getContentDescriptionForTrends(context, reproductionNumber.trend) + "\n" +
            context.getString(R.string.accessibility_statistics_card_navigation_information)
    }
}
