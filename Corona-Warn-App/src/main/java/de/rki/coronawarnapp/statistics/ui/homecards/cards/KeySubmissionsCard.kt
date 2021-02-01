package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsKeysubmissionsLayoutBinding
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.statistics.util.formatStatisticalValue
import de.rki.coronawarnapp.statistics.util.getContentDescriptionForTrends
import de.rki.coronawarnapp.util.formatter.getPrimaryLabel

class KeySubmissionsCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsKeysubmissionsLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout, parent
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
            item.onHelpAction.invoke(item.stats)
        }

        with(item.stats as KeySubmissionsStats) {
            primaryLabel.text = getPrimaryLabel(context)
            primaryValue.text = formatStatisticalValue(context, keySubmissions.value, keySubmissions.decimals)
            primaryValue.contentDescription = getPrimaryLabel(context) + formatStatisticalValue(context, keySubmissions.value, keySubmissions.decimals) + context.getString(R.string.statistics_card_submission_title)

            secondaryValue.text = formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals)
            secondaryValue.contentDescription = context.getString(R.string.statistics_card_infections_secondary_label ) + formatStatisticalValue(context, sevenDayAverage.value, sevenDayAverage.decimals) + context.getString(R.string.statistics_card_submission_title) + " " + getContentDescriptionForTrends(context, sevenDayAverage.trend)


            tertiaryValue.text = formatStatisticalValue(context, total.value, total.decimals)
            tertiaryValue.contentDescription = context.getString(R.string.statistics_card_infections_tertiary_label) + formatStatisticalValue(context, total.value, total.decimals) + context.getString(R.string.statistics_card_submission_title)
            trendArrow.setTrend(sevenDayAverage.trend, sevenDayAverage.trendSemantic)
        }
    }
}
