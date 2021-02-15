package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class DiaryLocationViewHolder(
    parent: ViewGroup
) : BaseAdapter.VH(R.layout.contact_diary_location_list_item, parent),
    BindableVH<DiaryLocationListItem, ContactDiaryLocationListItemBinding> {

    private var afterTextChangedListener: ((String) -> Unit)? = null
    private val circumStanceTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // NOOP
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // NOOP
        }

        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            afterTextChangedListener?.invoke(s.toString())
        }
    }

    override val viewBinding = lazy {
        ContactDiaryLocationListItemBinding.bind(itemView).apply {
            // EditText has no methods to clear TextWatchers, and we need to refresh them on bind.
            circumstances.addTextChangedListener(circumStanceTextWatcher)
        }
    }

    override val onBindData: ContactDiaryLocationListItemBinding.(
        key: DiaryLocationListItem,
        payloads: List<Any>
    ) -> Unit = { key, _ ->
        mainBox.apply {
            title = key.item.locationName
            isExpanded = key.selected
            contentDescription = key.contentDescription.get(context)
            setClickLabel(context.getString(key.clickLabel))
        }

        afterTextChangedListener = null
        circumstances.setText(key.visit?.circumstances ?: "")
        afterTextChangedListener = { key.onCircumstancesChanged(key, it) }

        infoButton.setOnClickListener { key.onCircumStanceInfoClicked() }
    }
}
