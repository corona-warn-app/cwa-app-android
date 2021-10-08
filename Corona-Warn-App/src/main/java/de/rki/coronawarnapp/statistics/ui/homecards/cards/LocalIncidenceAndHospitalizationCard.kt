package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsLocalIncidenceAndHospitalizationLayoutBinding
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.LocalIncidenceStats
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.statistics.local.storage.SelectedStatisticsLocation
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.statistics.util.getLocalizedSpannableString
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.BaseCheckInVH.Companion.setupMenu
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class LocalIncidenceAndHospitalizationCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<LocalStatisticsCardItem, HomeStatisticsCardsLocalIncidenceAndHospitalizationLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsLocalIncidenceAndHospitalizationLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsLocalIncidenceAndHospitalizationLayoutBinding.(
        item: LocalStatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<LocalStatisticsCardItem>().singleOrNull() ?: item

        with(curItem.stats as LocalIncidenceStats) {

            overflowMenuButton.setupMenu(R.menu.menu_statistics_local_incidence) {
                when (it.itemId) {
                    R.id.menu_information -> curItem.onClickListener(curItem.stats).let { true }
                    R.id.menu_remove_item -> curItem.onRemoveListener(curItem.stats).let { true }
                    else -> false
                }
            }

            sevenDayIncidenceAndHospitalizationCardContainer.contentDescription =
                buildAccessibilityStringForLocalIncidenceCard(curItem.stats, sevenDayIncidence, sevenDayHospitalization)

            locationLabel.text = when (selectedLocation) {
                is SelectedStatisticsLocation.SelectedDistrict ->
                    selectedLocation.district.districtName
                is SelectedStatisticsLocation.SelectedFederalState ->
                    context.getString(selectedLocation.federalState.labelStringRes)
            }

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

            primaryTrendArrow.setTrend(sevenDayIncidence.trend, sevenDayIncidence.trendSemantic)

            secondaryLabel.text = getPrimaryLabel(context)
            secondaryLabel.text = getLocalizedSpannableString(
                context,
                formatStatisticalValue(context, sevenDayHospitalization.value, sevenDayHospitalization.decimals)
            )
            secondaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_local_incidence_title))
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(
                    formatStatisticalValue(
                        context,
                        sevenDayHospitalization.value,
                        sevenDayHospitalization.decimals
                    )
                )
                .append(getContentDescriptionForTrends(context, sevenDayHospitalization.trend))

            secondaryTrendArrow.setTrend(sevenDayHospitalization.trend, sevenDayHospitalization.trendSemantic)
        }
    }

    private fun buildAccessibilityStringForLocalIncidenceCard(
        item: LocalStatsItem,
        sevenDayIncidence: KeyFigureCardOuterClass.KeyFigure,
        sevenDayHospitalization: KeyFigureCardOuterClass.KeyFigure
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
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithTrailingSpace(
                formatStatisticalValue(
                    context,
                    sevenDayHospitalization.value,
                    sevenDayHospitalization.decimals
                )
            )
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
