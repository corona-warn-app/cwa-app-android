package de.rki.coronawarnapp.contactdiary.ui.day.person

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.Person
import de.rki.coronawarnapp.databinding.ContactDiaryPersonListLineBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer

class ContactDiaryPersonListAdapter : BaseAdapter<ContactDiaryPersonListAdapter.CachedPersonViewHolder>(),
    AsyncDiffUtilAdapter<Person> {

    override val asyncDiffer: AsyncDiffer<Person> = AsyncDiffer(this)

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CachedPersonViewHolder =
        CachedPersonViewHolder(parent)

    override fun onBindBaseVH(holder: CachedPersonViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    class CachedPersonViewHolder(
        parent: ViewGroup
    ) : BaseAdapter.VH(R.layout.contact_diary_person_list_line, parent),
        BindableVH<Person, ContactDiaryPersonListLineBinding> {
        override val viewBinding = lazy { ContactDiaryPersonListLineBinding.bind(itemView) }

        override val onBindData: ContactDiaryPersonListLineBinding.(key: Person) -> Unit = { item ->
            contactDiaryPersonListLineName.text = item.fullName
        }
    }
}
