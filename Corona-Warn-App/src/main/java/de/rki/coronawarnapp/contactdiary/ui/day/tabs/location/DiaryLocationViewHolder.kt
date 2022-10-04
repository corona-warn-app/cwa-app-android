package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.widget.TextViewCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListItemBinding
import de.rki.coronawarnapp.ui.durationpicker.format
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class DiaryLocationViewHolder(
    parent: ViewGroup
) : BaseAdapter.VH(R.layout.contact_diary_location_list_item, parent),
    BindableVH<DiaryLocationListItem, ContactDiaryLocationListItemBinding> {

    override val viewBinding = lazy { ContactDiaryLocationListItemBinding.bind(itemView) }

    override val onBindData: ContactDiaryLocationListItemBinding.(
        item: DiaryLocationListItem,
        changes: List<Any>
    ) -> Unit = { initial, changes ->
        val item = changes.firstOrNull() as? DiaryLocationListItem ?: initial

        mainBox.apply {
            header.setOnClickListener {
                it.contentDescription = item.onClickDescription.get(context)
                it.sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
                item.onItemClick(item)
            }

            title = item.item.locationName
            isExpanded = item.selected
            contentDescription = item.contentDescription.get(context)
            setClickLabel(context.getString(item.clickLabel))
        }

        circumstances.apply {
            if (changes.isEmpty()) setInputText(item.visit?.circumstances ?: "")
            circumstances.setInputTextChangedListener { item.onCircumstancesChanged(item, it) }
            setInfoButtonClickListener { item.onCircumStanceInfoClicked() }
        }

        durationInput.apply {
            val duration = item.visit?.duration
            text = duration?.format()
            if (duration == null || duration.toMillis() == 0L) {
                text = context.getString(R.string.duration_dialog_default_value)
                setBackgroundResource(R.drawable.contact_diary_duration_background_default)
                TextViewCompat.setTextAppearance(this, R.style.bodyNeutral)
            } else {
                setBackgroundResource(R.drawable.contact_diary_duration_background_selected)
                TextViewCompat.setTextAppearance(this, R.style.body1)
            }
        }

        durationInput.setOnClickListener {
            it.hideKeyboard()
            circumstances.clearFocus()
            item.onDurationDialog(item, durationInput.text.toString())
        }
    }
}
