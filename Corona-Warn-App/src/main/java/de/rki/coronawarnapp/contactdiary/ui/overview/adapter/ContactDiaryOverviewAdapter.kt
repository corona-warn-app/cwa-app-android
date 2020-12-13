package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryElement
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryElement
import de.rki.coronawarnapp.databinding.IncludeContactDiaryOverviewItemBinding
import de.rki.coronawarnapp.databinding.IncludeSubmissionCountryItemBinding
import de.rki.coronawarnapp.ui.submission.SubmissionCountry



class ContactDiaryOverviewAdapter(private val onElementSelectionListener: (DefaultContactDiaryElement) -> Unit) :
    RecyclerView.Adapter<ContactDiaryOverviewAdapter.OverviewElementHolder>() {
    private var _elements = emptyList<DefaultContactDiaryElement>()

    fun setElements(elements: List<DefaultContactDiaryElement>) {
        _elements = elements
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OverviewElementHolder {
        val inflater = LayoutInflater.from(parent.context)
        return OverviewElementHolder(
            IncludeContactDiaryOverviewItemBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = _elements.size

    override fun onBindViewHolder(holder: OverviewElementHolder, position: Int) {
        holder.bind(_elements[position], onElementSelectionListener)
    }

    class OverviewElementHolder(private val viewDataBinding: IncludeContactDiaryOverviewItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        fun bind(
            element: DefaultContactDiaryElement,
            onElementSelectionListener: (DefaultContactDiaryElement) -> Unit
        ) {
            viewDataBinding.element = element
            viewDataBinding.executePendingBindings()

        }
    }
}
