package de.rki.coronawarnapp.covidcertificate.validation.ui.validationstart

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.util.collections.replaceAll

class DccCountryAdapter(context: Context) : ArrayAdapter<DccCountry>(
    context, R.layout.dcc_country_list_item
) {
    private val countries = mutableListOf<DccCountry>()

    override fun getCount(): Int = countries.size

    override fun getItem(position: Int): DccCountry = countries[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val textView: TextView = (convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.dcc_country_list_item, parent, false)) as TextView

        textView.text = getItem(position).displayName()
        return textView
    }

    fun update(newCountries: List<DccCountry>) {
        countries.replaceAll(newCountries)
        notifyDataSetChanged()
    }
}
