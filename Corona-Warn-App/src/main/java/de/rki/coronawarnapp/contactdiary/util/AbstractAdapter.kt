package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.HasStableId
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer

internal abstract class AbstractAdapter<T : HasStableId, U : BaseAdapter.VH> : BaseAdapter<U>(),
    AsyncDiffUtilAdapter<T> {
    override val asyncDiffer: AsyncDiffer<T> = AsyncDiffer(this)

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].stableId
}
