package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter.DurationClassification
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.common.setOnCheckedChangeListener
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryPersonListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

class DiaryPersonViewHolder(
    parent: ViewGroup
) : BaseAdapter.VH(R.layout.contact_diary_person_list_item, parent),
    BindableVH<DiaryPersonListItem, ContactDiaryPersonListItemBinding> {

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
        ContactDiaryPersonListItemBinding.bind(itemView).apply {
            // EditText has no methods to clear TextWatchers, and we need to refresh them on bind.
            circumstances.addTextChangedListener(circumStanceTextWatcher)
        }
    }

    override val onBindData: ContactDiaryPersonListItemBinding.(
        key: DiaryPersonListItem,
        payloads: List<Any>
    ) -> Unit = { key, _ ->
        mainBox.apply {
            title = key.item.fullName
            isExpanded = key.selected
            contentDescription = key.contentDescription.get(context)
            setClickLabel(context.getString(key.clickLabel))
        }

        durationGroup.apply {
            clearOnButtonCheckedListeners()
            when (key.personEncounter?.durationClassification) {
                DurationClassification.MORE_THAN_15_MINUTES -> check(R.id.duration_above_15)
                DurationClassification.LESS_THAN_15_MINUTES -> check(R.id.duration_below_15)
                null -> clearChecked()
            }
            setOnCheckedChangeListener { checkedId ->
                when (checkedId) {
                    R.id.duration_above_15 -> DurationClassification.MORE_THAN_15_MINUTES
                    R.id.duration_below_15 -> DurationClassification.LESS_THAN_15_MINUTES
                    else -> null
                }.let { key.onDurationChanged(key, it) }
            }
        }

        maskGroup.apply {
            clearOnButtonCheckedListeners()
            when (key.personEncounter?.withMask) {
                true -> check(R.id.mask_with)
                false -> check(R.id.mask_without)
                null -> clearChecked()
            }
            setOnCheckedChangeListener { checkedId ->
                when (checkedId) {
                    R.id.mask_with -> true
                    R.id.mask_without -> false
                    else -> null
                }.let { key.onWithMaskChanged(key, it) }
            }
        }

        environmentGroup.apply {
            clearOnButtonCheckedListeners()
            when (key.personEncounter?.wasOutside) {
                true -> check(R.id.environment_outside)
                false -> check(R.id.environment_inside)
                null -> clearChecked()
            }
            setOnCheckedChangeListener { checkedId ->
                when (checkedId) {
                    R.id.environment_outside -> true
                    R.id.environment_inside -> false
                    else -> null
                }.let { key.onWasOutsideChanged(key, it) }
            }
        }
        afterTextChangedListener = null
        circumstances.setText(key.personEncounter?.circumstances ?: "")
        afterTextChangedListener = { key.onCircumstancesChanged(key, it) }

        infoButton.setOnClickListener { key.onCircumStanceInfoClicked() }
    }
}
