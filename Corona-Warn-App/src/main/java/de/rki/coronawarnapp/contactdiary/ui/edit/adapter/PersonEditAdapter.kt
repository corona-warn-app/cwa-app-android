package de.rki.coronawarnapp.contactdiary.ui.edit.adapter

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.util.AbstractAdapter
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryEditListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

internal class PersonEditAdapter(
    private val clickLabelString: String,
    private val getContentDescriptionString: (ContactDiaryPerson) -> String,
    private val onItemClicked: (item: ContactDiaryPerson) -> Unit
) : AbstractAdapter<ContactDiaryPerson, PersonEditAdapter.ViewHolder>() {

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindBaseVH(holder: ViewHolder, position: Int, payloads: MutableList<Any>) =
        holder.bind(data[position], payloads)

    inner class ViewHolder(parent: ViewGroup) : BaseAdapter.VH(R.layout.contact_diary_edit_list_item, parent),
        BindableVH<ContactDiaryPerson, ContactDiaryEditListItemBinding> {
        override val viewBinding:
            Lazy<ContactDiaryEditListItemBinding> =
            lazy { ContactDiaryEditListItemBinding.bind(itemView) }

        override val onBindData:
            ContactDiaryEditListItemBinding.(item: ContactDiaryPerson, payloads: List<Any>) -> Unit =
            { person, _ ->
                name.text = person.fullName
                itemContainer.apply {
                    setOnClickListener { onItemClicked(person) }
                    contentDescription = getContentDescriptionString(person)
                    setClickLabel(clickLabelString)
                }
            }
    }
}
