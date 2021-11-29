package de.rki.coronawarnapp.dccticketing.ui.validationresult

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFragmentsBinding
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken

fun CovidCertificateValidationResultFragmentsBinding.setHeaderForState(result: DccTicketingResultToken.DccResult) =
    when (result) {
        DccTicketingResultToken.DccResult.PASS -> {
            toolbar.setTitle(R.string.dcc_ticketing_result_passed_title)
            headerImage.setImageResource(R.drawable.covid_certificate_validation_passed_header)
        }
        DccTicketingResultToken.DccResult.OPEN -> {
            toolbar.setTitle(R.string.dcc_ticketing_result_open_title)
            headerImage.setImageResource(R.drawable.covid_certificate_validation_open_header)
        }
        DccTicketingResultToken.DccResult.FAIL -> {
            toolbar.setTitle(R.string.dcc_ticketing_result_failed_title)
            headerImage.setImageResource(R.drawable.covid_certificate_validation_failed_header)
        }
    }

