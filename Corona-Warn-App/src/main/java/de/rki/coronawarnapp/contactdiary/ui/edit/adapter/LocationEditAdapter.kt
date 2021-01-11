package de.rki.coronawarnapp.contactdiary.ui.edit.adapter

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.util.AbstractAdapter
import de.rki.coronawarnapp.contactdiary.util.setClickLabel
import de.rki.coronawarnapp.databinding.ContactDiaryEditListItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH

internal class LocationEditAdapter(
    private val clickLabelString: String,
    private val getContentDescriptionString: (ContactDiaryLocation) -> String,
    private val onItemClicked: (item: ContactDiaryLocation) -> Unit
) : AbstractAdapter<ContactDiaryLocation, LocationEditAdapter.ViewHolder>() {

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun onBindBaseVH(holder: ViewHolder, position: Int, payloads: MutableList<Any>) =
        holder.bind(data[position], payloads)

    inner class ViewHolder(parent: ViewGroup) : BaseAdapter.VH(R.layout.contact_diary_edit_list_item, parent),
        BindableVH<ContactDiaryLocation, ContactDiaryEditListItemBinding> {
        override val viewBinding:
            Lazy<ContactDiaryEditListItemBinding> =
            lazy { ContactDiaryEditListItemBinding.bind(itemView) }

        override val onBindData:
            ContactDiaryEditListItemBinding.(item: ContactDiaryLocation, payloads: List<Any>) -> Unit =
            { location, _ ->
                name.text = location.locationName
                itemContainer.apply {
                    setOnClickListener { onItemClicked(location) }
                    contentDescription = getContentDescriptionString(location)
                    setClickLabel(clickLabelString)
                }
            }
    }
}
