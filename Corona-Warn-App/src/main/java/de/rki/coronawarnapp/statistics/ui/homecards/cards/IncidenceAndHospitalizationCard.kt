package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsIncidenceLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.IncidenceAndHospitalizationStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel
import de.rki.coronawarnapp.util.formatter.getSecondaryLabel
import java.time.Instant

class IncidenceAndHospitalizationCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<GlobalStatisticsCardItem, HomeStatisticsCardsIncidenceLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsIncidenceLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsIncidenceLayoutBinding.(
        item: GlobalStatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { orig, payloads ->
        val item = payloads.filterIsInstance<GlobalStatisticsCardItem>().lastOrNull() ?: orig

        infoStatistics.setOnClickListener {
            item.onClickListener(item.stats)
        }

        with(item.stats as IncidenceAndHospitalizationStats) {

            incidenceContainer.contentDescription =
                buildAccessibilityStringForIncidenceCard(item.stats, sevenDayIncidence, sevenDayIncidenceSecondary)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals)
            )

            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_explanation_seven_day_incidence_title))
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(
                    formatStatisticalValue(
                        context,
                        sevenDayIncidence.value,
                        sevenDayIncidence.decimals
                    )
                )
                .append(getContentDescriptionForTrends(context, sevenDayIncidence.trend))

            primaryTrendArrow.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)

            // Secondary
            val secondaryLabelText = getSecondaryLabel(
                context,
                Instant.ofEpochSecond(sevenDayIncidenceSecondary.updatedAt)
            )
            secondaryLabel.text = secondaryLabelText
            secondaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayIncidenceSecondary.value, sevenDayIncidenceSecondary.decimals)
            )

            secondaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_explanation_seven_day_incidence_title))
                .appendWithTrailingSpace(secondaryLabelText)
                .appendWithTrailingSpace(
                    formatStatisticalValue(
                        context,
                        sevenDayIncidenceSecondary.value,
                        sevenDayIncidenceSecondary.decimals
                    )
                )
                .append(getContentDescriptionForTrends(context, sevenDayIncidenceSecondary.trend))

            secondaryTrendArrow.setTrend(sevenDayIncidenceSecondary.trend, sevenDayIncidenceSecondary.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForIncidenceCard(
        item: IncidenceAndHospitalizationStats,
        sevenDayIncidence: KeyFigureCardOuterClass.KeyFigure,
        sevenDayIncidenceSecondary: KeyFigureCardOuterClass.KeyFigure,
    ): StringBuilder {
        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_explanation_seven_day_incidence_title))
            .appendWithLineBreak(context.getString(R.string.statistics_seven_day_hospitalization_nationwide_text))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithTrailingSpace(
                formatStatisticalValue(
                    context,
                    sevenDayIncidence.value,
                    sevenDayIncidence.decimals
                )
            )
            .appendWithTrailingSpace(context.getString(R.string.statistics_card_incidence_value_description))
            .appendWithLineBreak(getContentDescriptionForTrends(context, sevenDayIncidence.trend))
            .appendWithLineBreak(context.getString(R.string.statistics_seven_day_hospitalization_card_title))
            .appendWithTrailingSpace(
                getSecondaryLabel(context, Instant.ofEpochSecond(sevenDayIncidenceSecondary.updatedAt))
            )
            .appendWithTrailingSpace(
                formatStatisticalValue(
                    context,
                    sevenDayIncidenceSecondary.value,
                    sevenDayIncidenceSecondary.decimals
                )
            )
            .appendWithLineBreak(getContentDescriptionForTrends(context, sevenDayIncidenceSecondary.trend))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
