package de.rki.coronawarnapp.bugreporting.uploadhistory.ui

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.uploadhistory.LogUpload
import de.rki.coronawarnapp.databinding.BugreportingUploadHistoryItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.BindableVH
import org.joda.time.format.DateTimeFormat

class HistoryItemAdapter : BaseAdapter<HistoryItemAdapter.CachedKeyViewHolder>() {

    val data = mutableListOf<LogUpload>()

    override fun getItemCount(): Int = data.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CachedKeyViewHolder = CachedKeyViewHolder(parent)

    override fun onBindBaseVH(holder: CachedKeyViewHolder, position: Int, payloads: MutableList<Any>) {
        data[position].let {
            holder.bind(it)
        }
    }

    class CachedKeyViewHolder(
        val parent: ViewGroup
    ) : BaseAdapter.VH(R.layout.bugreporting_upload_history_item, parent),
        BindableVH<LogUpload, BugreportingUploadHistoryItemBinding> {

        override val viewBinding = lazy { BugreportingUploadHistoryItemBinding.bind(itemView) }

        override val onBindData: BugreportingUploadHistoryItemBinding.(
            item: LogUpload,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            title.text = FORMATTER.print(item.uploadedAt.toUserTimeZone())
            description.text = "ID ${item.id}"
        }
    }

    companion object {
        private val FORMATTER = DateTimeFormat.forPattern("yyyy.MM.dd - HH:mm:ss")
    }
}
