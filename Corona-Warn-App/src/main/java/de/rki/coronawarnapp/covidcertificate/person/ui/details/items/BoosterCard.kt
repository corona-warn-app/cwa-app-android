package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.PersonDetailsBoosterCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BoosterCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<BoosterCard.Item, PersonDetailsBoosterCardBinding>(
        layoutRes = R.layout.person_details_booster_card,
        parent = parent
    ) {
    override val viewBinding: Lazy<PersonDetailsBoosterCardBinding> = lazy {
        PersonDetailsBoosterCardBinding.bind(itemView)
    }

    override val onBindData: PersonDetailsBoosterCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        root.setOnClickListener { curItem.onClick() }

        title.text = curItem.title
        if (curItem.badgeVisible) {
            val boosterBadgeDrawable = ImageSpan(context, R.drawable.ic_badge_with_space, 2)
            if (title.text.isNotEmpty() && title.layout != null) {
                title.post {
                    val textOnFirstLine = title.text.subSequence(
                        title.layout.getLineStart(0), title.layout.getLineEnd(0)
                    )
                    val restOfText = title.text.subSequence(title.layout.getLineEnd(0), title.text.length)
                    val spannableString = SpannableString("$textOnFirstLine*$restOfText")
                    spannableString.setSpan(
                        boosterBadgeDrawable,
                        textOnFirstLine.length,
                        textOnFirstLine.length + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    title.text = spannableString
                }
            }
        }
        subtitle.text = curItem.subtitle
    }

    data class Item(
        val title: String,
        val subtitle: String,
        val badgeVisible: Boolean = true,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {

        override val stableId = Item::class.hashCode().toLong()
    }
}
