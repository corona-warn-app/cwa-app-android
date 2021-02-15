package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.ui.setOnClickListenerThrottled

class DiaryLocationViewHolder(
    parent: ViewGroup
) : BaseAdapter.VH(R.layout.contact_diary_location_list_item, parent),
    BindableVH<DiaryLocationListItem, ContactDiaryLocationListItemBinding> {

    override val viewBinding = lazy { ContactDiaryLocationListItemBinding.bind(itemView) }

    override val onBindData: ContactDiaryLocationListItemBinding.(
        key: DiaryLocationListItem,
        payloads: List<Any>
    ) -> Unit = { key, _ ->
        mainBox.apply {
            header.setOnClickListenerThrottled {
                it.contentDescription = key.onClickDescription.get(context)
                it.sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
                key.onItemClick(key)
            }

            title = key.item.locationName
            isExpanded = key.selected
            contentDescription = key.contentDescription.get(context)
            setClickLabel(context.getString(key.clickLabel))
        }

        circumstances.apply {
            setInputText(key.visit?.circumstances ?: "")
            circumstances.setInputTextChangedListener { key.onCircumstancesChanged(key, it) }
            setInfoButtonClickListener { key.onCircumStanceInfoClicked() }
        }
    }
}
