package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsSevenDayHospitalizationLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.SevenDayHospitalizationStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class SevenDayHospitalizationCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<GlobalStatisticsCardItem, HomeStatisticsCardsSevenDayHospitalizationLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {
    override val viewBinding = lazy {
        HomeStatisticsCardsSevenDayHospitalizationLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsSevenDayHospitalizationLayoutBinding.(
        item: GlobalStatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { orig, payloads ->
        val item = payloads.filterIsInstance<GlobalStatisticsCardItem>().singleOrNull() ?: orig
        infoStatistics.setOnClickListener {
            item.onClickListener(item.stats)
        }

        with(item.stats as SevenDayHospitalizationStats) {
            sevenDayHospitalizationCardContainer.contentDescription =
                buildAccessibilityStringForSevenDayHospitalizationCard(
                    item.stats,
                    sevenDayValue
                )

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayValue.value, sevenDayValue.decimals)
            )
            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(
                    context.getString(R.string.statistics_seven_day_hospitalization_card_title)
                )
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(
                    formatStatisticalValue(
                        context,
                        sevenDayValue.value,
                        sevenDayValue.decimals
                    )
                )
                .append(getContentDescriptionForTrends(context, sevenDayValue.trend))

            trendArrow.setTrend(sevenDayValue.trend, sevenDayValue.trendSemantic)

        }
    }

    private fun buildAccessibilityStringForSevenDayHospitalizationCard(
        item: GlobalStatsItem,
        sevenDayValue: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {
        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_seven_day_hospitalization_card_title))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithTrailingSpace(
                formatStatisticalValue(
                    context,
                    sevenDayValue.value,
                    sevenDayValue.decimals
                )
            )
            .appendWithTrailingSpace(context.getString(R.string.statistics_seven_day_hospitalization_nationwide_text))
            .appendWithLineBreak(getContentDescriptionForTrends(context, sevenDayValue.trend))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
