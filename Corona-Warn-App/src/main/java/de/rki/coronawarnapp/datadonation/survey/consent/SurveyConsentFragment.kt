package de.rki.coronawarnapp.datadonation.survey.consent

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.SurveyConsentFragmentBinding
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SurveyConsentFragment : Fragment(R.layout.survey_consent_fragment), AutoInject {

    private val navArgs by navArgs<SurveyConsentFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SurveyConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SurveyConsentViewModel.Factory
            factory.create(navArgs.SurveyType)
        }
    )

    private val binding: SurveyConsentFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { vm.onBackButtonPressed() }
            surveyNextButton.setOnClickListener { vm.onNextButtonPressed() }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {
                is SurveyConsentNavigationEvents.NavigateBack ->
                    activity?.onBackPressed()
                is SurveyConsentNavigationEvents.NavigateToWebView ->
                    Toast.makeText(requireContext(), "Open ${it.url} (WIP, next PR))", Toast.LENGTH_SHORT).show()
            }
        }

        vm.showLoadingIndicator.observe2(this) {
            // TODO
        }

        vm.showErrorDialog.observe2(this) {
            showDialog(it.msgRes)
        }
    }

    private fun showDialog(@StringRes stringRes: Int) {
        context?.let {
            val dialog = DialogHelper.DialogInstance(
                context = it,
                title = R.string.datadonation_details_survey_consent_error_dialog_title,
                message = stringRes,
                positiveButton = R.string.datadonation_details_survey_consent_error_dialog_pos_button,
                positiveButtonFunction = { vm.onBackButtonPressed() },
                cancelable = false
            )
            DialogHelper.showDialog(dialog)
        }
    }
}
