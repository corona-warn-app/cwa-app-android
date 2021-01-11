package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewCountryListEntryFlagItemBinding
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.ui.view.CountryFlagsAdapter.CountryFlagViewHolder
import de.rki.coronawarnapp.util.lists.BindableVH
import java.text.Collator

class CountryListView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val adapterCountryFlags = CountryFlagsAdapter()
    private val grid: RecyclerView
    private val countryNames: TextView

    var countries: List<Country> = defaultCountryList
        set(value) {
            field = value.sortedWith { a, b ->
                // Sort country list alphabetically
                Collator.getInstance().compare(a.getLabel(context), b.getLabel(context))
            }.also { countries ->
                adapterCountryFlags.countryList = countries
                countryNames.text = countries.joinToString(", ") { it.getLabel(context) }
            }
        }

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_country_list_entry_flag_container, this)

        grid = findViewById<RecyclerView>(R.id.flagGrid).apply {
            layoutManager = GridLayoutManager(context, FLAG_COLUMNS)
            adapter = adapterCountryFlags
        }
        countryNames = findViewById(R.id.country_list_entry_label)
    }

    // Helper to allow for null in data binding
    fun setCountryList(countries: List<Country>?) {
        this.countries = countries ?: defaultCountryList
    }

    companion object {
        private const val FLAG_COLUMNS = 8
    }
}

private class CountryFlagsAdapter : BaseAdapter<CountryFlagViewHolder>() {

    var countryList: List<Country> = defaultCountryList
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = countryList.size

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CountryFlagViewHolder = CountryFlagViewHolder(parent)

    override fun onBindBaseVH(holder: CountryFlagViewHolder, position: Int, payloads: MutableList<Any>) =
        holder.bind(countryList[position])

    class CountryFlagViewHolder(val parent: ViewGroup) : VH(
        R.layout.view_country_list_entry_flag_item, parent
    ), BindableVH<Country, ViewCountryListEntryFlagItemBinding> {

        override val viewBinding: Lazy<ViewCountryListEntryFlagItemBinding> = lazy {
            ViewCountryListEntryFlagItemBinding.bind(itemView)
        }

        override val onBindData: ViewCountryListEntryFlagItemBinding.(
            key: Country,
            payloads: List<Any>
        ) -> Unit = { item, _ ->
            countryListEntryImage.setImageResource(item.iconRes)
        }
    }
}

private val defaultCountryList = listOf(Country.DE)
