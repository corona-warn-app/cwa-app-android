package de.rki.coronawarnapp.familytest.ui.selection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestRegistrationSelectionBinding
import de.rki.coronawarnapp.ui.submission.qrcode.consent.SubmissionConsentBackNavArg.BackToTestRegistrationSelection
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class TestRegistrationSelectionFragment : Fragment(R.layout.fragment_test_registration_selection), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val navArgs by navArgs<TestRegistrationSelectionFragmentArgs>()
    private val viewModel: TestRegistrationSelectionViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as TestRegistrationSelectionViewModel.Factory
            factory.create(navArgs.coronaTestQrCode)
        }
    )
    private val binding: FragmentTestRegistrationSelectionBinding by viewBinding()
    private val navOptions = NavOptions.Builder().setPopUpTo(R.id.testRegistrationSelectionFragment, true).build()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is TestRegistrationSelectionNavigationEvents.NavigateBack -> {
                    popBackStack()
                }
                is TestRegistrationSelectionNavigationEvents.NavigateToPerson -> {
                    findNavController().navigate(
                        TestRegistrationSelectionFragmentDirections
                            .actionTestRegistrationSelectionFragmentToSubmissionConsentFragment(
                                coronaTestQrCode = it.coronaTestQRCode,
                                navigateBackTo = BackToTestRegistrationSelection,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            ),
                        navOptions
                    )
                }
                is TestRegistrationSelectionNavigationEvents.NavigateToDeletionWarning -> {
                    findNavController().navigate(
                        TestRegistrationSelectionFragmentDirections
                            .actionTestRegistrationSelectionFragmentToSubmissionDeletionWarningFragment(
                                testRegistrationRequest = it.testRegistrationRequest,
                                popToTestCategorySelection = true,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            )
                    )
                }
                is TestRegistrationSelectionNavigationEvents.NavigateToFamily -> {
                    findNavController().navigate(
                        TestRegistrationSelectionFragmentDirections
                            .actionTestRegistrationSelectionFragmentToFamilyTestConsentFragment(
                                coronaTestQrCode = it.coronaTestQRCode,
                                comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
                            ),
                        navOptions
                    )
                }
            }
        }

        with(binding) {
            toolbar.setNavigationOnClickListener { viewModel.onNavigateBack() }
            person.setOnClickListener { viewModel.onNavigateToPerson() }
            family.setOnClickListener { viewModel.onNavigateToFamily() }
        }
    }
}
