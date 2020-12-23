package de.rki.coronawarnapp.test.keydownload.ui

import android.text.format.Formatter
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestKeydownloadAdapterLineBinding
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.ui.setGone
import org.joda.time.format.DateTimeFormat

class KeyFileDownloadAdapter(
    private val deleteAction: (CachedKeyListItem) -> Unit
) : BaseAdapter<KeyFileDownloadAdapter.CachedKeyViewHolder>(), AsyncDiffUtilAdapter<CachedKeyListItem> {

    init {
        setHasStableIds(true)
    }

    override val asyncDiffer: AsyncDiffer<CachedKeyListItem> = AsyncDiffer(this)

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CachedKeyViewHolder = CachedKeyViewHolder(parent)

    override fun onBindBaseVH(holder: CachedKeyViewHolder, position: Int, payloads: MutableList<Any>) {
        val item = data[position]
        holder.itemView.setOnLongClickListener {
            deleteAction(item)
            true
        }
        holder.bind(item)
    }

    class CachedKeyViewHolder(
        val parent: ViewGroup
    ) : BaseAdapter.VH(
        R.layout.fragment_test_keydownload_adapter_line, parent
    ), BindableVH<CachedKeyListItem, FragmentTestKeydownloadAdapterLineBinding> {

        override val viewBinding = lazy { FragmentTestKeydownloadAdapterLineBinding.bind(itemView) }

        override val onBindData: FragmentTestKeydownloadAdapterLineBinding.(
            key: CachedKeyListItem,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            locationInfo.text = item.info.location.identifier

            val shortSize = Formatter.formatShortFileSize(context, item.fileSize)
            typeInfo.text = when (item.info.type) {
                CachedKeyInfo.Type.LOCATION_DAY -> "Day ($shortSize)"
                CachedKeyInfo.Type.LOCATION_HOUR -> "Hour ($shortSize)"
            }
            timeInfo.text = when (item.info.type) {
                CachedKeyInfo.Type.LOCATION_DAY -> "${item.info.day}"
                CachedKeyInfo.Type.LOCATION_HOUR -> "${item.info.day} ${item.info.hour!!.hourOfDay}:00"
            }
            creationData.text = item.info.createdAt.toString(DOWNLOAD_TIME_FORMATTER)
            creationLabel.setGone(!item.info.isDownloadComplete)
            creationData.setGone(!item.info.isDownloadComplete)
            progressIndicator.setGone(item.info.isDownloadComplete)
        }
    }

    companion object {
        private val DOWNLOAD_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSS")
    }
}
