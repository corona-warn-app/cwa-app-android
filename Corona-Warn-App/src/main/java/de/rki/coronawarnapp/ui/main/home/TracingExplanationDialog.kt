package de.rki.coronawarnapp.ui.main.home

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.DialogHelper

interface TracingExplanationDialog {

    companion object {
        fun create(context: Context, activeTracingDaysInRetentionPeriod: Long) {
            // get all text strings and the current active tracing time
            val infoPeriodLogged =
                context.getString(R.string.risk_details_information_body_period_logged)
            val infoPeriodLoggedAssessment =
                context.getString(
                    R.string.risk_details_information_body_period_logged_assessment,
                    activeTracingDaysInRetentionPeriod.toString()
                )
            val infoFAQ = context.getString(R.string.risk_details_explanation_dialog_faq_body)

            // display the dialog

            DialogHelper.DialogInstance(
                context,
                context.getString(R.string.risk_details_explanation_dialog_title),
                "$infoPeriodLogged\n\n$infoPeriodLoggedAssessment\n\n$infoFAQ",
                context.getString(R.string.errors_generic_button_positive),
                null,
                null,
                {
                    LocalData.tracingExplanationDialogWasShown(true)
                },
                {}
            ).let { DialogHelper.showDialog(it) }
        }
    }
}
