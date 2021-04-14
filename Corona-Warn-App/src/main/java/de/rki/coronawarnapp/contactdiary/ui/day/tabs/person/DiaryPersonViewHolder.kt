package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.common.setOnCheckedChangeListener
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryPersonListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class DiaryPersonViewHolder(
    parent: ViewGroup
) : BaseAdapter.VH(R.layout.contact_diary_person_list_item, parent),
    BindableVH<DiaryPersonListItem, ContactDiaryPersonListItemBinding> {

    override val viewBinding = lazy { ContactDiaryPersonListItemBinding.bind(itemView) }

    override val onBindData: ContactDiaryPersonListItemBinding.(
        item: DiaryPersonListItem,
        changes: List<Any>
    ) -> Unit = { initial, changes ->
        val item = changes.firstOrNull() as? DiaryPersonListItem ?: initial

        mainBox.apply {
            header.setOnClickListener {
                hideKeyboard()
                it.contentDescription = item.onClickDescription.get(context)
                it.sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
                item.onItemClick(item)
            }

            title = item.item.fullName
            isExpanded = item.selected
            contentDescription = item.contentDescription.get(context)
            setClickLabel(context.getString(item.clickLabel))
        }

        durationGroup.apply {
            clearOnButtonCheckedListeners()
            when (item.personEncounter?.durationClassification) {
                DurationClassification.MORE_THAN_15_MINUTES -> check(R.id.duration_above_15)
                DurationClassification.LESS_THAN_15_MINUTES -> check(R.id.duration_below_15)
                null -> clearChecked()
            }
            setOnCheckedChangeListener { checkedId ->
                hideKeyboard()
                when (checkedId) {
                    R.id.duration_above_15 -> DurationClassification.MORE_THAN_15_MINUTES
                    R.id.duration_below_15 -> DurationClassification.LESS_THAN_15_MINUTES
                    else -> null
                }.let { item.onDurationChanged(item, it) }
            }
        }

        maskGroup.apply {
            clearOnButtonCheckedListeners()
            when (item.personEncounter?.withMask) {
                true -> check(R.id.mask_with)
                false -> check(R.id.mask_without)
                null -> clearChecked()
            }
            setOnCheckedChangeListener { checkedId ->
                hideKeyboard()
                when (checkedId) {
                    R.id.mask_with -> true
                    R.id.mask_without -> false
                    else -> null
                }.let { item.onWithMaskChanged(item, it) }
            }
        }

        environmentGroup.apply {
            clearOnButtonCheckedListeners()
            when (item.personEncounter?.wasOutside) {
                true -> check(R.id.environment_outside)
                false -> check(R.id.environment_inside)
                null -> clearChecked()
            }
            setOnCheckedChangeListener { checkedId ->
                hideKeyboard()
                when (checkedId) {
                    R.id.environment_outside -> true
                    R.id.environment_inside -> false
                    else -> null
                }.let { item.onWasOutsideChanged(item, it) }
            }
        }

        circumstances.apply {
            // When data changes, we get that via payload
            // To not update the edittext while typing, only the the text input on the first "bind"
            if (changes.isEmpty()) setInputText(item.personEncounter?.circumstances ?: "")
            circumstances.setInputTextChangedListener { item.onCircumstancesChanged(item, it) }
            setInfoButtonClickListener { item.onCircumstanceInfoClicked() }
        }
    }
}
