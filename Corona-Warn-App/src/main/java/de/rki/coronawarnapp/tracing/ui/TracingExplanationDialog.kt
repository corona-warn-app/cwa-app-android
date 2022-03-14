package de.rki.coronawarnapp.tracing.ui

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.home.HomeFragment
import de.rki.coronawarnapp.util.DialogHelper
import javax.inject.Inject

class TracingExplanationDialog @Inject constructor(
    private val homeFragment: HomeFragment
) {
    private val context: Context
        get() = homeFragment.requireContext()

    fun show(maxEncounterAgeInDays: Int, onPositive: () -> Unit) {

        val infoPeriodLogged =
            context.getString(R.string.risk_details_information_body_period_logged, maxEncounterAgeInDays)
        val infoPeriodLoggedAssessment =
            context.getString(R.string.risk_details_information_body_period_logged_assessment)
        val infoFAQ = context.getString(R.string.risk_details_explanation_dialog_faq_body)

        val data = DialogHelper.DialogInstance(
            context = context,
            title = context.getString(R.string.risk_details_explanation_dialog_title),
            message = "$infoPeriodLogged\n$infoPeriodLoggedAssessment\n\n$infoFAQ",
            positiveButton = context.getString(R.string.errors_generic_button_positive),
            negativeButton = null,
            cancelable = false,
            positiveButtonFunction = onPositive,
            negativeButtonFunction = {}
        )
        DialogHelper.showDialog(data)
    }
}
