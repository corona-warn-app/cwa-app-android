package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ViewCountryListEntryFlagItemBinding
import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.util.lists.BindableVH
import kotlinx.android.synthetic.main.view_country_list_entry_flag_container.view.*
import java.text.Collator

class CountryList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    private var attributeSet = attrs
    private var _list: List<Country>? = null
    var list: List<Country>?
        get() = _list
        set(value) {
            _list = value
            buildAllLists()
        }

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_country_list_entry_flag_container, this)
    }

    /**
     * Cleans the view and rebuilds the list of countries. Presets already selected countries
     */
    private fun buildAllLists() {
        list
            ?.map { country ->
                context.getString(country.labelRes) to country.iconRes
            }
            ?.sortedWith { a, b ->
                Collator.getInstance().compare(a.first, b.first)
            }
        list?.let { prepareFlags(it) }
    }

    private fun prepareFlags(list: List<Country>) {
        val adapterCountryFlags = _list?.let { CountryAdapterFlags(it) }
        var countryNames = ""
        context.withStyledAttributes(attributeSet, R.styleable.SimpleStepEntry) {
            flagGrid.layoutManager = GridLayoutManager(context, FLAG_COLUMNS)
            flagGrid.adapter = adapterCountryFlags
            val iterator: Iterator<Country> = list.iterator()
            while (iterator.hasNext()) {
                countryNames += resources.getString(iterator.next().labelRes)
                if (iterator.hasNext()) countryNames += ", "
            }
            country_list_entry_label.text = countryNames
        }
    }

    companion object {
        const val FLAG_COLUMNS = 8
    }
}

/**
 * Adapter for the country flags grid
 */
class CountryAdapterFlags internal constructor(
    countryList: List<Country>
) :
    BaseCountryFlagAdapter<CountryAdapterFlags.CountryFlagViewHolder>() {
    // total number of cells

    private val countryList: List<Country>? = countryList
    override fun getItemCount(): Int = countryList!!.size

    override fun getItemId(position: Int): Long = countryList?.get(position)!!.iconRes.hashCode().toLong()

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): CountryFlagViewHolder =
        CountryFlagViewHolder(parent)

    override fun onBindBaseVH(holder: CountryFlagViewHolder, position: Int) {
        val item = countryList!![position]
        holder.bind(item)
    }

    /**
     * CustomViewHolder for the country flags grid
     */
    class CountryFlagViewHolder(
        val parent: ViewGroup
    ) : BaseCountryFlagAdapter.VH(
        R.layout.view_country_list_entry_flag_item, parent
    ), BindableVH<Country, ViewCountryListEntryFlagItemBinding> {

        override val viewBinding: Lazy<ViewCountryListEntryFlagItemBinding> = lazy {
            ViewCountryListEntryFlagItemBinding.bind(itemView)
        }

        override val onBindData: ViewCountryListEntryFlagItemBinding.(key: Country) -> Unit = { item ->
            countryListEntryImage.setImageResource(item.iconRes)
        }
    }
}

abstract class BaseCountryFlagAdapter<T : BaseCountryFlagAdapter.VH> : RecyclerView.Adapter<T>() {
    @CallSuper
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        return onCreateBaseVH(parent, viewType)
    }

    abstract fun onCreateBaseVH(parent: ViewGroup, viewType: Int): T

    @CallSuper
    final override fun onBindViewHolder(holder: T, position: Int) {
        onBindBaseVH(holder, position)
    }

    abstract fun onBindBaseVH(holder: T, position: Int)

    abstract class VH(@LayoutRes layoutRes: Int, parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
    ) {
        val context: Context = parent.context
    }
}
