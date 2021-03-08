package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.LogUpload
import de.rki.coronawarnapp.databinding.BugreportingUploadHistoryItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.BindableVH
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

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
            itemView.setOnClickListener {
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            context.getString(R.string.debugging_debuglog_share_log_title),
                            """
                                ${context.getString(R.string.debugging_debuglog_share_log_title)}
                                ${title.text}
                                ${description.text} 
                            """.trimIndent()
                        )
                    )
                } catch (e: Throwable) {
                    Timber.e(e, "Failed to copy ID to clipboard.")
                }
            }
        }
    }

    companion object {
        private val FORMATTER = DateTimeFormat.forPattern("yyyy.MM.dd - HH:mm:ss")
    }
}
