package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsVaccinatedCompletelyLayoutBinding
import de.rki.coronawarnapp.server.protocols.internal.stats.KeyFigureCardOuterClass
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.PersonsVaccinatedCompletelyStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatPercentageValue
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithLineBreak
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel
import java.util.Locale

class PersonsVaccinatedCompletelyCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<GlobalStatisticsCardItem, HomeStatisticsCardsVaccinatedCompletelyLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {
    override val viewBinding = lazy {
        HomeStatisticsCardsVaccinatedCompletelyLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    private val currentSelectedLocale =
        ConfigurationCompat.getLocales(resources.configuration).get(0) ?: Locale.getDefault()

    override val onBindData: HomeStatisticsCardsVaccinatedCompletelyLayoutBinding.(
        item: GlobalStatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { orig, payloads ->
        val item = payloads.filterIsInstance<GlobalStatisticsCardItem>().lastOrNull() ?: orig

        infoStatistics.setOnClickListener {
            item.onClickListener(item.stats)
        }

        with(item.stats as PersonsVaccinatedCompletelyStats) {
            personsVaccinatedCompletelyContainer.contentDescription =
                buildAccessibilityStringForPersonsVaccinatedCompletelyCard(item.stats, allDoses, total)

            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = formatPercentageValue(allDoses.value, currentSelectedLocale)
            primaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(getPrimaryLabel(context))
                .appendWithTrailingSpace(formatPercentageValue(allDoses.value, currentSelectedLocale))
                .append(context.getString(R.string.statistics_vaccinated_completely_card_title))

            secondaryValue.text = formatStatisticalValue(context, total.value, total.decimals)
            secondaryValue.contentDescription = StringBuilder()
                .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
                .appendWithTrailingSpace(formatStatisticalValue(context, total.value, total.decimals))
                .append(context.getString(R.string.statistics_vaccinated_completely_card_title))
        }
    }

    private fun buildAccessibilityStringForPersonsVaccinatedCompletelyCard(
        item: GlobalStatsItem,
        firstDose: KeyFigureCardOuterClass.KeyFigure,
        total: KeyFigureCardOuterClass.KeyFigure
    ): StringBuilder {

        return StringBuilder()
            .appendWithTrailingSpace(context.getString(R.string.accessibility_statistics_card_announcement))
            .appendWithLineBreak(context.getString(R.string.statistics_vaccinated_completely_card_title))
            .appendWithLineBreak(context.getString(R.string.statistics_nationwide_text))
            .appendWithTrailingSpace(item.getPrimaryLabel(context))
            .appendWithLineBreak(formatPercentageValue(firstDose.value, currentSelectedLocale))
            .appendWithTrailingSpace(context.getString(R.string.statistics_card_infections_tertiary_label))
            .appendWithTrailingSpace(formatStatisticalValue(context, total.value, total.decimals))
            .append(context.getString(R.string.accessibility_statistics_card_navigation_information))
    }
}
