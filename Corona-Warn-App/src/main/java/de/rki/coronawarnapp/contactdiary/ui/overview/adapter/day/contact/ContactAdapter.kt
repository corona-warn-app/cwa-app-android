package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.contact

import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewNestedListItemBinding
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import java.time.Duration

class ContactAdapter : BaseAdapter<ContactAdapter.ContactItemViewHolder>() {

    private val dataList: MutableList<ContactItem.Data> = mutableListOf()

    fun setItems(dataList: List<ContactItem.Data>) {
        this.dataList.clearAndAddAll(dataList)
        notifyDataSetChanged()
    }

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): ContactItemViewHolder = ContactItemViewHolder(parent)

    override fun onBindBaseVH(holder: ContactItemViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(dataList[position], payloads)
    }

    override fun getItemCount(): Int = dataList.size

    inner class ContactItemViewHolder(parent: ViewGroup) :
        VH(R.layout.contact_diary_overview_nested_list_item, parent),
        BindableVH<ContactItem.Data, ContactDiaryOverviewNestedListItemBinding> {
        override val viewBinding: Lazy<ContactDiaryOverviewNestedListItemBinding> =
            lazy { ContactDiaryOverviewNestedListItemBinding.bind(itemView) }

        override val onBindData: ContactDiaryOverviewNestedListItemBinding.(
            item: ContactItem.Data,
            payloads: List<Any>
        ) -> Unit = { key, _ ->
            contactDiaryOverviewElementImage.setImageResource(key.drawableId)
            contactDiaryOverviewElementName.text = key.name
            contactDiaryOverviewElementNestedBody.contentDescription = when (key.type) {
                ContactItem.Type.LOCATION -> context.getString(R.string.accessibility_location, key.name)
                ContactItem.Type.PERSON -> context.getString(R.string.accessibility_person, key.name)
            }
            val attributes = getAttributes(key.duration, key.attributes, key.circumstances)
            if (attributes.isNotEmpty()) {
                contactDiaryOverviewElementAttributes.text = attributes
            } else contactDiaryOverviewElementAttributes.visibility = View.GONE
        }

        private fun getAttributes(duration: Duration?, resources: List<Int>?, circumstances: String?): String =
            mutableListOf<String>().apply {
                duration?.run {
                    if (duration != Duration.ZERO) {
                        val durationSuffix = context.getString(R.string.contact_diary_location_visit_duration_hour)
                        add(toReadableDuration(suffix = durationSuffix))
                    }
                }
                resources?.run { forEach { add(context.getString(it)) } }
                circumstances?.run { add(this) }
            }.filter { it.isNotEmpty() }.joinToString()
    }
}
