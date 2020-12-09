package de.rki.coronawarnapp.contactdiary.ui.day.place

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.Location
import de.rki.coronawarnapp.databinding.ContactDiaryPlaceListLineBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer

class ContactDiaryPlaceListAdapter : BaseAdapter<ContactDiaryPlaceListAdapter.CachedPlaceViewHolder>(),
    AsyncDiffUtilAdapter<Location> {

    override val asyncDiffer: AsyncDiffer<Location> = AsyncDiffer(this)

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CachedPlaceViewHolder = CachedPlaceViewHolder(parent)

    override fun onBindBaseVH(holder: CachedPlaceViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class CachedPlaceViewHolder(
        parent: ViewGroup
    ) : BaseAdapter.VH(R.layout.contact_diary_place_list_line, parent),
        BindableVH<Location, ContactDiaryPlaceListLineBinding> {
        override val viewBinding = lazy { ContactDiaryPlaceListLineBinding.bind(itemView) }

        override val onBindData: ContactDiaryPlaceListLineBinding.(key: Location) -> Unit = { item ->
            contactDiaryPlaceListLineName.text = item.locationName
        }
    }
}
