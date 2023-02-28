package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.coordinatorlayout.widget.CoordinatorLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultAdapter
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFragmentsBinding
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.util.lists.decorations.RecylerViewPaddingDecorator

class CovidCertificateValidationResultView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CoordinatorLayout(context, attrs) {

    private val binding: CovidCertificateValidationResultFragmentsBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.covid_certificate_validation_result_fragments, this, true)
        binding = CovidCertificateValidationResultFragmentsBinding.bind(this)
    }

    fun setHeaderForState(
        dccTicketingResult: DccTicketingResultToken.DccResult? = null,
        dccValidationState: DccValidation.State? = null,
        ruleCount: Int = 0
    ) {
        val state = dccTicketingResult ?: dccValidationState
        with(binding) {
            when (state) {
                DccValidation.State.PASSED -> {
                    if (ruleCount > 0) {
                        toolbar.setTitle(R.string.validation_rules_result_valid_header)
                        headerImage.setImageResource(R.drawable.covid_certificate_validation_passed_header)
                    } else {
                        toolbar.setTitle(R.string.validation_open_title)
                        headerImage.setImageResource(R.drawable.covid_certificate_validation_open_header)
                    }
                }

                DccValidation.State.OPEN -> {
                    toolbar.setTitle(R.string.validation_open_title)
                    headerImage.setImageResource(R.drawable.covid_certificate_validation_open_header)
                }

                DccValidation.State.TECHNICAL_FAILURE,
                DccValidation.State.FAILURE -> {
                    toolbar.setTitle(R.string.validation_failed_title)
                    headerImage.setImageResource(R.drawable.covid_certificate_validation_failed_header)
                }
            }
        }
    }

    fun populateList(
        covidCertificateAdapter: ValidationResultAdapter? = null,
        ticketingAdapter: de.rki.coronawarnapp.dccticketing.ui.validationresult.ValidationResultAdapter? = null,
        addPadding: Boolean = false
    ) {
        binding.list.apply {
            adapter = covidCertificateAdapter ?: ticketingAdapter
            if (addPadding) {
                val padding = R.dimen.standard_16
                addItemDecoration(
                    RecylerViewPaddingDecorator(
                        topPadding = padding,
                        leftPadding = padding,
                        rightPadding = padding
                    )
                )
            }
        }
    }

    fun offsetChange() = binding.appBarLayout.onOffsetChange { _, subtitleAlpha ->
        binding.headerImage.alpha = subtitleAlpha
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.toolbar.setOnClickListener(l)
    }

    fun setNavigationOnClickListener(l: OnClickListener) {
        binding.toolbar.setNavigationOnClickListener(l)
    }
}
