package de.rki.coronawarnapp.familytest.ui.consent

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.hideKeyboard
import de.rki.coronawarnapp.databinding.FragmentFamilyTestConsentBinding
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class FamilyTestConsentFragment : Fragment(R.layout.fragment_family_test_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<FamilyTestConsentFragmentArgs>()
    private val viewModel: FamilyTestConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as FamilyTestConsentViewModel.Factory
            factory.create(navArgs.coronaTestQrCode)
        }
    )
    private val qrcodeSharedViewModel: QrcodeSharedViewModel by navGraphViewModels(R.id.nav_graph)
    private val binding: FragmentFamilyTestConsentBinding by viewBinding()
    private val navOptions = NavOptions.Builder().setPopUpTo(R.id.familyTestConsentFragment, true).build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onNavigateBack()
                }
            }
        )

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is FamilyTestConsentNavigationEvents.NavigateBack -> {
                    binding.root.hideKeyboard()
                    findNavController().navigate(
                        FamilyTestConsentFragmentDirections
                            .actionFamilyTestConsentFragmentToTestRegistrationSelectionFragment(
                                navArgs.coronaTestQrCode
                            )
                    )
                }
                is FamilyTestConsentNavigationEvents.NavigateClose -> {
                    binding.root.hideKeyboard()
                    goBack()
                }
                is FamilyTestConsentNavigationEvents.NavigateToDataPrivacy -> findNavController().navigate(
                    FamilyTestConsentFragmentDirections.actionFamilyTestConsentFragmentToInformationPrivacyFragment()
                )
                is FamilyTestConsentNavigationEvents.NavigateToCertificateRequest -> findNavController().navigate(
                    NavGraphDirections.actionRequestCovidCertificateFragment(
                        testRegistrationRequest = it.coronaTestQRCode,
                        coronaTestConsent = it.consentGiven,
                        allowTestReplacement = it.allowReplacement,
                        personName = it.personName
                    ),
                    navOptions
                )
                else -> Unit
            }
        }

        viewModel.registrationState.observe2(this) { state ->
            val isWorking = state is TestRegistrationStateProcessor.State.Working
            binding.apply {
                consentButton.isLoading = isWorking
            }
            when (state) {
                TestRegistrationStateProcessor.State.Idle,
                TestRegistrationStateProcessor.State.Working -> Unit
                is TestRegistrationStateProcessor.State.Error -> {
                    state.showExceptionDialog(this) { popBackStack() }
                }
                is TestRegistrationStateProcessor.State.TestRegistered -> findNavController().navigate(
                    NavGraphDirections.actionSubmissionTestResultPendingFragment(
                        testIdentifier = state.test.identifier,
                        comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                    ),
                    navOptions
                )
            }
        }

        binding.apply {
            nameInputEdit.doAfterTextChanged {
                viewModel.nameChanged(it.toString())
            }
            nameInputEdit.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        if (viewModel.isSubmittable.value == true) {
                            binding.consentButton.performClick()
                        }
                        false
                    }
                    else -> true
                }
            }
            nameInputEdit.setText(qrcodeSharedViewModel.familyTestPersonName)
            toolbar.setNavigationOnClickListener {
                viewModel.onNavigateClose()
            }
            dataPrivacyMoreInfo.setOnClickListener {
                viewModel.onDataPrivacyClick()
            }
            consentButton.setOnClickListener {
                viewModel.onConsentButtonClick()
            }
        }

        viewModel.isSubmittable.observe2(this) {
            binding.consentButton.isActive = it
        }
    }

    private fun goBack() {
        if (navArgs.comesFromDispatcherFragment) {
            findNavController().navigate(FamilyTestConsentFragmentDirections.actionGlobalMainFragment())
        } else popBackStack()
    }
}
