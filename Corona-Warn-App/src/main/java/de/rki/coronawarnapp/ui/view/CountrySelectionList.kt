package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants
import java.util.Locale

class CountrySelectionList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private var _countryList: List<String>? = null
    var countryList: List<String>?
        get() {
            return _countryList?.map { it.toLowerCase(Locale.ROOT) }
        }
        set(value) {
            _countryList = value
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
        countryList
            ?.map { countryCode ->
                val countryNameResourceId = context.resources.getIdentifier(
                    "country_name_$countryCode",
                    "string",
                    context.packageName
                )
                Pair(countryCode, context.getString(countryNameResourceId))
            }
            ?.sortedBy { it.second }
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

        entry.findViewById<ImageView>(R.id.img_country_flag)
            .setImageDrawable(countryFlagDrawable)

        entry.findViewById<TextView>(R.id.label_country_name).text = countryName
    }
}
