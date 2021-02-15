package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.contactdiary.util.AbstractAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.ui.setOnClickListenerThrottled

internal class ContactDiaryPersonListAdapter : AbstractAdapter<DiaryPersonListItem, DiaryPersonViewHolder>(),
    AsyncDiffUtilAdapter<DiaryPersonListItem> {

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int) = DiaryPersonViewHolder(parent)

    override fun onBindBaseVH(holder: DiaryPersonViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = data[position]
        holder.itemView.setOnClickListenerThrottled {
            it.contentDescription = item.onClickDescription.get(holder.context)
            it.sendAccessibilityEvent(AccessibilityEvent.CONTENT_CHANGE_TYPE_CONTENT_DESCRIPTION)
            item.onItemClick(item)
        }
        holder.bind(item, payloads)
    }
}
