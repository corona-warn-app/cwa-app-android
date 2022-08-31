package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsOccupiedIntensiveCareBedsBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.OccupiedIntensiveCareStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatPercentageValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel
import java.util.Locale

class OccupiedIntensiveCareCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<GlobalStatisticsCardItem, HomeStatisticsCardsOccupiedIntensiveCareBedsBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {
    override val viewBinding = lazy {
        HomeStatisticsCardsOccupiedIntensiveCareBedsBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    private val currentSelectedLocale =
        ConfigurationCompat.getLocales(resources.configuration).get(0) ?: Locale.getDefault()

    override val onBindData: HomeStatisticsCardsOccupiedIntensiveCareBedsBinding.(
        item: GlobalStatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { orig, payloads ->
        val item = payloads.filterIsInstance<GlobalStatisticsCardItem>().lastOrNull() ?: orig
        infoStatistics.setOnClickListener {
            item.onClickListener(item.stats)
        }

        with(item.stats as OccupiedIntensiveCareStats) {
            occupiedIntensiveCareContainer.contentDescription =
                buildAccessibilityStringForOccupiedIntensiveCareCard(
                    item.stats,
                    occupationRatio
                )

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = formatPercentageValue(occupationRatio.value, currentSelectedLocale)
            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_occupied_intensive_care_card_title))
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(formatPercentageValue(occupationRatio.value, currentSelectedLocale))
                .append(getContentDescriptionForTrends(context, occupationRatio.trend))
            trendArrow.setTrend(occupationRatio.trend, occupationRatio.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForOccupiedIntensiveCareCard(
        item: GlobalStatsItem,
        occupationRatio: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {
        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_occupied_intensive_care_card_title))
            .appendWithTrailingSpace(context.getString(R.string.statistics_nationwide_text))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithTrailingSpace(formatPercentageValue(occupationRatio.value, currentSelectedLocale))
            .appendWithTrailingSpace(context.getString(R.string.statistics_occupied_intensive_care_beds_text))
            .appendWithLineBreak(getContentDescriptionForTrends(context, occupationRatio.trend))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
