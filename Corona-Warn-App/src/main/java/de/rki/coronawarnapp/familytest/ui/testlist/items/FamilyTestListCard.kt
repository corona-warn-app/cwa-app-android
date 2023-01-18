package de.rki.coronawarnapp.familytest.ui.testlist.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.FamilyTestListCardBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.FamilyTestListAdapter
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListCard.Item
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDate
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUIFormat
import de.rki.coronawarnapp.util.list.Swipeable
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class FamilyTestListCard(parent: ViewGroup) :
    FamilyTestListAdapter.FamilyTestListVH<Item, FamilyTestListCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ),
    Swipeable {

    private var latestItem: Item? = null

    override fun onSwipe(holder: RecyclerView.ViewHolder, direction: Int) {
        latestItem?.let { it.onSwipeItem(it.familyCoronaTest, holder.bindingAdapterPosition) }
    }

    override val viewBinding = lazy {
        FamilyTestListCardBinding
            .inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: FamilyTestListCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        latestItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        latestItem?.let {

            when (it.familyCoronaTest.type) {
                BaseCoronaTest.Type.RAPID_ANTIGEN -> {
                    findings.setText(R.string.family_tests_cards_rapid_title)
                    date.text = resources.getString(
                        R.string.family_tests_cards_rapid_date,
                        it.familyCoronaTest.coronaTest.testTakenAt.toDate().toUIFormat(context)
                    )
                }
                BaseCoronaTest.Type.PCR -> {
                    findings.setText(R.string.family_tests_cards_pcr_title)
                    date.text = resources.getString(
                        R.string.family_tests_cards_pcr_date,
                        it.familyCoronaTest.coronaTest.getFormattedRegistrationDate()
                    )
                }
            }

            when {
                it.familyCoronaTest.isPositive -> positive()
                it.familyCoronaTest.isNegative -> negative()
                it.familyCoronaTest.isPending -> pending(it.familyCoronaTest.type)
                it.familyCoronaTest.isInvalid -> invalid()
                else -> invalid() // fallback
            }

            title.text = it.familyCoronaTest.personName
            notificationBadge.isVisible = it.familyCoronaTest.hasBadge
            itemView.setOnClickListener { _ ->
                it.onClickAction(item)
            }
        }
    }

    private fun FamilyTestListCardBinding.negative() {
        status.setTextColor(resources.getColor(R.color.colorTextSemanticGreen, null))
        status.setText(R.string.ag_homescreen_card_status_negative)
        icon.setImageResource(R.drawable.ic_test_result_illustration_negative_card)
        body.isVisible = false
        targetDisease.isVisible = true
    }

    private fun FamilyTestListCardBinding.positive() {
        status.setTextColor(resources.getColor(R.color.colorTextSemanticRed, null))
        status.setText(R.string.ag_homescreen_card_status_positiv)
        icon.setImageResource(R.drawable.ic_test_result_illustration_positive_card)
        body.isVisible = false
        targetDisease.isVisible = true
    }

    private fun FamilyTestListCardBinding.pending(type: BaseCoronaTest.Type) {
        status.setTextColor(resources.getColor(R.color.colorOnPrimary, null))
        status.setText(R.string.ag_homescreen_card_status_no_result)
        icon.setImageResource(R.drawable.ic_test_result_illustration_pending_card)
        when (type) {
            BaseCoronaTest.Type.PCR -> body.setText(R.string.family_tests_cards_pcr_pending_body)
            BaseCoronaTest.Type.RAPID_ANTIGEN -> body.setText(R.string.family_tests_cards_rapid_pending_body)
        }
        body.isVisible = true
        targetDisease.isVisible = false
    }

    private fun FamilyTestListCardBinding.invalid() {
        status.setTextColor(resources.getColor(R.color.colorOnPrimary, null))
        status.setText(R.string.ag_homescreen_card_status_error)
        icon.setImageResource(R.drawable.ic_test_result_illustration_invalid_card)
        body.isVisible = true
        body.setText(R.string.family_tests_cards_invalid_body)
        targetDisease.isVisible = false
        date.isVisible = false
    }

    data class Item(
        override val familyCoronaTest: FamilyCoronaTest,
        val onClickAction: (Item) -> Unit,
        val onSwipeItem: (FamilyCoronaTest, Int) -> Unit,
    ) : FamilyTestListItem, HasPayloadDiffer
}
