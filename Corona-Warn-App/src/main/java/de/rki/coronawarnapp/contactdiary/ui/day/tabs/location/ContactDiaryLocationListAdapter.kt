package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.view.ViewGroup
import de.rki.coronawarnapp.contactdiary.util.AbstractAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter

internal class ContactDiaryLocationListAdapter :
    AbstractAdapter<DiaryLocationListItem, DiaryLocationViewHolder>(),
    AsyncDiffUtilAdapter<DiaryLocationListItem> {

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int) = DiaryLocationViewHolder(parent)

    override fun onBindBaseVH(holder: DiaryLocationViewHolder, position: Int, payloads: MutableList<Any>) {
        holder.bind(data[position])
    }
}
