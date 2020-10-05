package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.Country
import java.text.Collator

class CountryList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private var _list: List<Country>? = null
    var list: List<Country>?
        get() = _list
        set(value) {
            _list = value
            buildList()
        }

    init {
        orientation = VERTICAL
    }

    /**
     * Cleans the view and rebuilds the list of countries. Presets already selected countries
     */
    private fun buildList() {
        this.removeAllViews()
        list
            ?.map { country ->
                context.getString(country.labelRes) to country.iconRes
            }
            ?.sortedWith { a, b ->
                Collator.getInstance().compare(a.first, b.first)
            }
            ?.forEachIndexed { index, (label, iconRes) ->
                inflate(context, R.layout.view_country_list_entry, this)
                val child = this.getChildAt(index)
                child.apply {
                    findViewById<ImageView>(R.id.country_list_entry_image).setImageResource(iconRes)
                    findViewById<TextView>(R.id.country_list_entry_label).text = label
                }
            }
    }
}
