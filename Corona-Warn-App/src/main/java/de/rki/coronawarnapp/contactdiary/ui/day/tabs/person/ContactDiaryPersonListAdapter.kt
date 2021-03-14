package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.view.ViewGroup
import de.rki.coronawarnapp.contactdiary.util.AbstractAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter

internal class ContactDiaryPersonListAdapter :
    AbstractAdapter<DiaryPersonListItem, DiaryPersonViewHolder>(),
    AsyncDiffUtilAdapter<DiaryPersonListItem> {

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int) = DiaryPersonViewHolder(parent)

    override fun onBindBaseVH(holder: DiaryPersonViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(data[position], payloads)
    }
}
