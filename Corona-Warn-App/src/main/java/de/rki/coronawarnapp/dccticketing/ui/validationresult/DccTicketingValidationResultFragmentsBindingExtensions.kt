package de.rki.coronawarnapp.dccticketing.ui.validationresult

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFragmentsBinding

fun CovidCertificateValidationResultFragmentsBinding.setHeaderForState(result: String?) =
    when (result) {
        "OK" -> {

            toolbar.setTitle(R.string.validation_rules_result_valid_header)
            headerImage.setImageResource(R.drawable.covid_certificate_validation_passed_header)
        }
        "CHK" -> {
            toolbar.setTitle(R.string.validation_open_title)
            headerImage.setImageResource(R.drawable.covid_certificate_validation_open_header)
        }
        else -> {
            toolbar.setTitle(R.string.validation_failed_title)
            headerImage.setImageResource(R.drawable.covid_certificate_validation_failed_header)
        }
    }
