package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsLocalIncidenceLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class LocalIncidenceCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsLocalIncidenceLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsLocalIncidenceLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsLocalIncidenceLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<StatisticsCardItem>().singleOrNull() ?: item

        with(curItem.stats as LocalIncidenceStats) {

            overflowMenuButton.setupMenu(R.menu.menu_statistics_local_incidence) {
                when (it.itemId) {
                    R.id.menu_information -> curItem.onClickListener(curItem.stats).let { true }
                    R.id.menu_remove_item -> curItem.onRemoveListener(curItem.stats).let { true }
                    else -> false
                }
            }

            incidenceContainer.contentDescription =
                buildAccessibilityStringForLocalIncidenceCard(curItem.stats, sevenDayIncidence)

            locationLabel.text = selectedDistrict.district.districtName

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayIncidence.value, sevenDayIncidence.decimals)
            )

            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_local_incidence_title))
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(
                    formatStatisticalValue(
                        context,
                        sevenDayIncidence.value,
                        sevenDayIncidence.decimals
                    )
                )
                .append(getContentDescriptionForTrends(context, sevenDayIncidence.trend))

            trendArrow.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForLocalIncidenceCard(
        item: StatsItem,
        sevenDayIncidence: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {

        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_card_local_incidence_title))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithTrailingSpace(
                formatStatisticalValue(
                    context,
                    sevenDayIncidence.value,
                    sevenDayIncidence.decimals
                )
            )
            .appendWithTrailingSpace(context.getString(R.string.statistics_card_local_incidence_value_description))
            .appendWithLineBreak(getContentDescriptionForTrends(context, sevenDayIncidence.trend))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
