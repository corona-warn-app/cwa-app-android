package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsVaccinatedOnceLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.PersonsVaccinatedOnceStats
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatPercentageValue
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class PersonsVaccinatedOnceCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsVaccinatedOnceLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsVaccinatedOnceLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsVaccinatedOnceLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

        infoStatistics.setOnClickListener {
            item.onClickListener(item.stats)
        }

        with(item.stats as PersonsVaccinatedOnceStats) {
            personsVaccinatedOnceContainer.contentDescription =
                buildAccessibilityStringForPersonsVaccinatedOnceCard(item.stats, firstDose, total)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = formatPercentageValue(firstDose.value)
            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(formatStatisticalValue(context, firstDose.value, firstDose.decimals))
                .append(context.getString(R.string.statistics_vaccinated_once_card_title))

            secondaryValue.text = formatStatisticalValue(context, total.value, total.decimals)
            secondaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
                .appendWithTrailingSpace(formatStatisticalValue(context, total.value, total.decimals))
                .append(context.getString(R.string.statistics_vaccinated_once_card_title))
        }
    }

    private fun buildAccessibilityStringForPersonsVaccinatedOnceCard(
        item: StatsItem,
        firstDose: KeyFigureCardOuterClass.KeyFigure,
        total: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {

        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_vaccinated_once_card_title))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithLineBreak(formatStatisticalValue(context, firstDose.value, firstDose.decimals))
            .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
            .appendWithTrailingSpace(formatStatisticalValue(context, total.value, total.decimals))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
