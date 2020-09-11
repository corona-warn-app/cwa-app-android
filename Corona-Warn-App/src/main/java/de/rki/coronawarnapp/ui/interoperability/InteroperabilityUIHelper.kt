package de.rki.coronawarnapp.ui.interoperability

import androidx.fragment.app.FragmentActivity
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.viewmodel.InteroperabilityViewModel
import de.rki.coronawarnapp.util.DialogHelper

/**
 * Handles the UI logic for Interoperability (layouts: include_country_list)
 * @see InteroperabilityConfigurationFragment
 * @see OnboardingInteroperabilityFragment
 */
data class InteroperabilityUIHelper(
    private val interoperabilityViewModel: InteroperabilityViewModel,
    private val fragmentActivity: FragmentActivity
) {

    /**
     * Updates the viewmodel and saves the given country code if selected. Displays a dialog if user
     * wants to disable the country
     */
    fun handleCountrySelected(countryCode: String, selected: Boolean, showDialog: Boolean) {
        if (!selected) {
            if (showDialog) {
                showCountryDisabledDialog {
                    interoperabilityViewModel.updateSelectedCountryCodes(countryCode, false)
                }
            }
            interoperabilityViewModel.updateSelectedCountryCodes(countryCode, false)
        } else {
            interoperabilityViewModel.updateSelectedCountryCodes(countryCode, true)
        }
    }

    /**
     * Updates the viewmodel and saves all countries if selected. Displays a dialog if user
     * wants to disable all countries
     */
    fun handleAllCountrySwitchChanged(checked: Boolean, showDialog: Boolean = true) {
        if (!checked) {
            if (showDialog) {
                showCountryDisabledDialog {
                    interoperabilityViewModel.overwriteSelectedCountries(false)
                }
            } else {
                interoperabilityViewModel.overwriteSelectedCountries(false)
            }
        } else {
            interoperabilityViewModel.overwriteSelectedCountries(true)
        }
    }

    fun showCountryDisabledDialog(deactivate: () -> Unit) {
        val dialog = DialogHelper.DialogInstance(
            fragmentActivity,
            R.string.interoperability_country_disabled_dialog_headline,
            R.string.interoperability_country_disabled_dialog_body,
            R.string.interoperability_country_disabled_dialog_positive_button,
            R.string.interoperability_country_disabled_dialog_negative_button,
            false,
            {},
            deactivate
        )
        DialogHelper.showDialog(dialog)
    }
}
