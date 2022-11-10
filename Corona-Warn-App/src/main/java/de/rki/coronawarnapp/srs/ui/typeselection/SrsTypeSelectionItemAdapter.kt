package de.rki.coronawarnapp.srs.ui.typeselection

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.SrsTypeSelectionAdapterItemBinding
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import javax.inject.Inject

class SrsTypeSelectionItemAdapter @Inject constructor() : BaseAdapter<SrsTypeSelectionItemAdapter.VH>() {

    private val internalData = mutableListOf<SrsTypeSelectionItem>()

    var data: List<SrsTypeSelectionItem>
        get() = internalData.toList()
        set(value) {
            internalData.clear()
            internalData.addAll(value)
            notifyDataSetChanged()
        }

    var onItemClickListener: (SrsTypeSelectionItem) -> Unit = {}

    override fun getItemCount(): Int = internalData.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.apply {
            val item = internalData[position]
            bind(item)
            itemView.setOnClickListener { onItemClickListener(item) }
        }
    }

    class VH(parent: ViewGroup) : BaseAdapter.VH(R.layout.srs_type_selection_adapter_item, parent) {
        private val viewBinding: SrsTypeSelectionAdapterItemBinding =
            SrsTypeSelectionAdapterItemBinding.bind(itemView)

        fun bind(item: SrsTypeSelectionItem) = viewBinding.apply {
            typeName.isChecked = item.checked
            typeName.setText(
                when (item.submissionType) {
                    SrsSubmissionType.SRS_SELF_TEST,
                    SrsSubmissionType.SRS_REGISTERED_RAT -> R.string.srs_rat_registered_no_result_text

                    SrsSubmissionType.SRS_UNREGISTERED_RAT -> R.string.srs_rat_not_registered_text
                    SrsSubmissionType.SRS_REGISTERED_PCR -> R.string.srs_pcr_registered_no_result_text
                    SrsSubmissionType.SRS_UNREGISTERED_PCR -> R.string.srs_pcr_not_registered_text
                    SrsSubmissionType.SRS_RAPID_PCR -> R.string.srs_rapid_pcr_text
                    SrsSubmissionType.SRS_OTHER -> R.string.srs_other_text
                }
            )
        }
    }
}
