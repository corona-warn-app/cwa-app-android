package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ValidationRulesResultValidScreenBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DccValidationPassedFragment : Fragment(R.layout.validation_rules_result_valid_screen), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: DccValidationPassedViewmodel by cwaViewModels { viewModelFactory }
    private val binding: ValidationRulesResultValidScreenBinding by viewBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            validationResultFaqTv.also {
                val faqLinkLabel = getString(R.string.validation_rules_result_valid_result_faq_link_label)
                val faqLink = getString(R.string.validation_rules_result_valid_result_faq_link)

                val reopenLinkLabel = getString(R.string.validation_rules_result_valid_result_reopen_link_label)
                val reopenLink = getString(R.string.validation_rules_result_valid_result_reopen_link)

                it.text = getString(R.string.validation_rules_result_valid_result_faq, faqLinkLabel, reopenLinkLabel)
                it.setUrl(faqLinkLabel, faqLink)
                it.setUrl(reopenLinkLabel, reopenLink)
            }
        }
    }
}
