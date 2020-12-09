package de.rki.coronawarnapp.contactdiary.ui.day.location

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.Location
import de.rki.coronawarnapp.databinding.ContactDiaryLocationListLineBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer

class ContactDiaryLocationListAdapter : BaseAdapter<ContactDiaryLocationListAdapter.CachedLocationViewHolder>(),
    AsyncDiffUtilAdapter<Location> {

    override val asyncDiffer: AsyncDiffer<Location> = AsyncDiffer(this)

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CachedLocationViewHolder =
        CachedLocationViewHolder(parent)

    override fun onBindBaseVH(holder: CachedLocationViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class CachedLocationViewHolder(
        parent: ViewGroup
    ) : BaseAdapter.VH(R.layout.contact_diary_location_list_line, parent),
        BindableVH<Location, ContactDiaryLocationListLineBinding> {
        override val viewBinding = lazy { ContactDiaryLocationListLineBinding.bind(itemView) }

        override val onBindData: ContactDiaryLocationListLineBinding.(key: Location) -> Unit = { item ->
            contactDiaryLocationListLineName.text = item.locationName
        }
    }
}
