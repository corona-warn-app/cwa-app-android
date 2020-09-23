package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import de.rki.coronawarnapp.R
import java.text.Collator
import java.util.Locale

class CountryList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private var _list: List<String>? = null
    var list: List<String>?
        get() {
            return _list?.map { it.toLowerCase(Locale.ROOT) }
        }
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
            ?.map { countryCode ->
                val countryNameResourceId = context.resources.getIdentifier(
                    "country_name_$countryCode",
                    "string",
                    context.packageName
                )
                Pair(countryCode, context.getString(countryNameResourceId))
            }
            ?.sortedWith(Comparator { a, b ->
                Collator.getInstance().compare(a.second, b.second)
            })
            ?.forEachIndexed { index, country ->
                inflate(context, R.layout.view_country_list_entry, this)
                val child = this.getChildAt(index)
                this.setEntryValues(child, country.first, country.second)
            }
    }

    /**
     * Sets the values of the views of each entry in the list
     * @param entry the view of the current entry
     * @param countryCode needed to determine which country is used for the current entry
     * @param selected sets the status of the switch for the current entry
     */
    private fun setEntryValues(
        entry: View,
        countryCode: String,
        countryName: String
    ) {

        // get drawable (flag of country) resource if dynamically based on country code
        val countryFlagImageDrawableId = context.resources.getIdentifier(
            "ic_country_$countryCode",
            "drawable",
            context.packageName
        )

        val countryFlagDrawable = ResourcesCompat
            .getDrawable(context.resources, countryFlagImageDrawableId, null)

        entry.findViewById<ImageView>(R.id.country_list_entry_image)
            .setImageDrawable(countryFlagDrawable)

        entry.findViewById<TextView>(R.id.country_list_entry_label).text = countryName
    }
}
