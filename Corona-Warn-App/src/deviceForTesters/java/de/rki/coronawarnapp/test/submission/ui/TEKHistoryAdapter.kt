package de.rki.coronawarnapp.test.submission.ui

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestSubmissionTekhistoryLineBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import okio.ByteString.Companion.toByteString

class TEKHistoryAdapter : BaseAdapter<TEKHistoryAdapter.VH>(), AsyncDiffUtilAdapter<TEKHistoryItem> {

    init {
        setHasStableIds(true)
    }

    override val asyncDiffer: AsyncDiffer<TEKHistoryItem> = AsyncDiffer(this)

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = data[position].stableId

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) {
        val item = data[position]
        holder.bind(item)
    }

    class VH(
        val parent: ViewGroup
    ) : BaseAdapter.VH(
        R.layout.fragment_test_submission_tekhistory_line, parent
    ), BindableVH<TEKHistoryItem, FragmentTestSubmissionTekhistoryLineBinding> {

        override val viewBinding = lazy { FragmentTestSubmissionTekhistoryLineBinding.bind(itemView) }

        override val onBindData: FragmentTestSubmissionTekhistoryLineBinding.(
            item: TEKHistoryItem,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            val key = item.key
            primary.text = """
                rollingStartIntervalNumber=${key.rollingStartIntervalNumber}  rollingPeriod=${key.rollingPeriod}
                transmissionRiskLevel=${key.transmissionRiskLevel}  reportType=${key.reportType}
                daysSinceOnsetOfSymptoms=${key.daysSinceOnsetOfSymptoms}
            """.trimIndent()
            secondary.text = """
                keyData=${key.keyData.toByteString().base64()}
                obtainedAt=${item.obtainedAt}
            """.trimIndent()
        }
    }
}
