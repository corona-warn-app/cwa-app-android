package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsKeysubmissionsLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class KeySubmissionsCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsKeysubmissionsLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsKeysubmissionsLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsKeysubmissionsLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        infoStatistics.setOnClickListener {
            item.onClickListener(item.stats)
        }

        with(item.stats as KeySubmissionsStats) {

            keysubmissionsContainer.contentDescription =
                buildAccessibilityStringForKeySubmissionsCard(item.stats, keySubmissions, sevenDayAverage, total)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = formatStatisticalValue(context, keySubmissions.value, keySubmissions.decimals)
            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(formatStatisticalValue(context, keySubmissions.value, keySubmissions.decimals))
                .append(context.getString(R.string.statistics_card_submission_title))

            secondaryValue.text = formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals)
            secondaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_secondary_label))
                .appendWithTrailingSpace(
                    formatStatisticalValue(
                        context,
                        sevenDayAverage.value,
                        sevenDayAverage.decimals
                    )
                )
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_submission_title))
                .append(getContentDescriptionForTrends(context, sevenDayAverage.trend))

            tertiaryValue.text = formatStatisticalValue(context, total.value, total.decimals)
            tertiaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
                .appendWithTrailingSpace(formatStatisticalValue(context, total.value, total.decimals))
                .append(context.getString(R.string.statistics_card_submission_title))

            trendArrow.setTrend(sevenDayAverage.trend, sevenDayAverage.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForKeySubmissionsCard(
        item: StatsItem,
        keySubmissions: KeyFigureCardOuterClass.KeyFigure,
        sevenDayAverage: KeyFigureCardOuterClass.KeyFigure,
        total: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {

        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_card_submission_title))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithLineBreak(formatStatisticalValue(context, keySubmissions.value, keySubmissions.decimals))
            .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_secondary_label))
            .appendWithTrailingSpace(formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals))
            .appendWithLineBreak(getContentDescriptionForTrends(context, sevenDayAverage.trend))
            .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
            .appendWithTrailingSpace(formatStatisticalValue(context, total.value, total.decimals))
            .appendWithLineBreak(context.getString(R.string.statistics_card_submission_bottom_text))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
