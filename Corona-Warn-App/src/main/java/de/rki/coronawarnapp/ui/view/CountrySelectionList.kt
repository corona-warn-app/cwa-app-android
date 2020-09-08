package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import kotlinx.coroutines.runBlocking
import java.util.Locale

class CountrySelectionList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    private var countryList: List<String>? = null

    init {
        orientation = VERTICAL

        runBlocking {
            // Get all supported countries from backend and build country list
            countryList =
                ApplicationConfigurationService
                    .asyncRetrieveApplicationConfiguration().supportedCountriesList
            buildList()
        }
    }

    /**
     * Saves the changes of a country
     */
    private fun countrySelected(countryCode: String, enabled: Boolean) {
        if (enabled) {
            InteroperabilityRepository.updateSelectedCountryCodes(countryCode)
        }
    }

    /**
     * Cleans the view and rebuilds the list of countries. Presets already selected countries
     */
    private fun buildList() {
        this.removeAllViews()
        val savedSelectedCountries = InteroperabilityRepository.getSelectedCountryCodes()
        countryList?.map { it.toLowerCase(Locale.ROOT) }?.forEachIndexed { index, countryCode ->
            inflate(context, R.layout.view_country_list_entry, this)
            val child = this.getChildAt(index)
            val isAlreadySelected = savedSelectedCountries.contains(countryCode)
            this.setEntryValues(child, countryCode, isAlreadySelected)
        }

    }

    /**
     * Sets the values of the views of each entry in the list
     * @param entry the view of the current entry
     * @param countryCode needed to determine which country is used for the current entry
     * @param selected sets the status of the switch for the current entry
     */
    private fun setEntryValues(entry: View, countryCode: String, selected: Boolean = false) {
        // get string (Name of country) resource if dynamically based on country code
        val countryNameResourceId = context.resources.getIdentifier(
            "country_name_$countryCode",
            "string",
            context.packageName
        )

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

        entry.findViewById<TextView>(R.id.label_country_name).text =
            context.getString(countryNameResourceId)


        val countrySwitch = entry.findViewById<Switch>(R.id.switch_country_enabled)
        countrySwitch.isEnabled = selected

        countrySwitch.setOnCheckedChangeListener { view, checked ->
            this.countrySelected(countryCode, checked)
        }
    }
}

