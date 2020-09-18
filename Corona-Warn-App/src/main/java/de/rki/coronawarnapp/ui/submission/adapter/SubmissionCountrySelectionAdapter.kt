package de.rki.coronawarnapp.ui.submission.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.databinding.IncludeSubmissionCountryItemBinding
import de.rki.coronawarnapp.ui.submission.SubmissionCountry

class SubmissionCountrySelectionAdapter(private val onCountrySelectionListener: (SubmissionCountry) -> Unit) :
    RecyclerView.Adapter<SubmissionCountrySelectionAdapter.SubmissionCountryHolder>() {
    private var _countries = emptyList<SubmissionCountry>()

    fun setCountries(countries: List<SubmissionCountry>) {
        _countries = countries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionCountryHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SubmissionCountryHolder(
            IncludeSubmissionCountryItemBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = _countries.size

    override fun onBindViewHolder(holder: SubmissionCountryHolder, position: Int) {
        holder.bind(_countries[position], onCountrySelectionListener)
    }

    class SubmissionCountryHolder(private val viewDataBinding: IncludeSubmissionCountryItemBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        fun bind(
            country: SubmissionCountry,
            onCountrySelectionListener: (SubmissionCountry) -> Unit
        ) {
            viewDataBinding.submissionCountry = country
            viewDataBinding.executePendingBindings()

            viewDataBinding.submissionCountrySelectorCountryBody.setOnClickListener {
                country.selected = !country.selected
                onCountrySelectionListener(country)
            }

            viewDataBinding.submissionCountrySelectorCountryRadiobutton.setOnCheckedChangeListener { _, isChecked ->
                country.selected = isChecked
                onCountrySelectionListener(country)
            }
        }
    }
}
