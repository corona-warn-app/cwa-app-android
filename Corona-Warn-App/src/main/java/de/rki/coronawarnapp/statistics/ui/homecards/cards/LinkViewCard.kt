package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsLinkCardLayoutBinding
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter
import de.rki.coronawarnapp.util.StringBuilderExtension.appendWithTrailingSpace

class LinkViewCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<LinkCardItem, HomeStatisticsCardsLinkCardLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsLinkCardLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsLinkCardLayoutBinding.(
        item: LinkCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<LinkCardItem>().lastOrNull() ?: item
        if (curItem.isEol) {
            itemView.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            itemView.layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.width_300)
        }
        cardLinkContainer.contentDescription = buildAccessibilityString()
        infoStatistics.isGone = curItem.isEol
        infoStatistics.setOnClickListener { curItem.onClickListener(curItem.linkStats) }
        linkButton.setOnClickListener { curItem.openLink(curItem.linkStats.url) }
    }

    private fun buildAccessibilityString() = StringBuilder()
        .appendWithTrailingSpace(context.getString(R.string.pandemic_radar_card_title))
        .appendWithTrailingSpace(context.getString(R.string.pandemic_radar_card_subtitle))
        .appendWithTrailingSpace(context.getString(R.string.pandemic_radar_card_message))
        .appendWithTrailingSpace(context.getString(R.string.pandemic_Radar_card_button_text))
}
