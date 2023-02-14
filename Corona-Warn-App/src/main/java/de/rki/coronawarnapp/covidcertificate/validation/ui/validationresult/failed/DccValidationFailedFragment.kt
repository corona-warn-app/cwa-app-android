package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.failed

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultAdapter
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.setHeaderForState
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFragmentsBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.lists.decorations.RecylerViewPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DccValidationFailedFragment : Fragment(R.layout.covid_certificate_validation_result_fragments) {

    @Inject lateinit var factory: DccValidationFailedViewModel.Factory
    private val binding by viewBinding<CovidCertificateValidationResultFragmentsBinding>()
    private val args by navArgs<DccValidationFailedFragmentArgs>()
    private val viewModel: DccValidationFailedViewModel by assistedViewModel {
        factory.create(validation = args.validation, containerId = args.containerId)
    }

    @Inject lateinit var validationResultAdapter: ValidationResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            setHeaderForState(args.validation.state)
            list.apply {
                adapter = validationResultAdapter
                addItemDecoration(
                    RecylerViewPaddingDecorator(
                        topPadding = R.dimen.standard_16,
                        leftPadding = R.dimen.standard_16,
                        rightPadding = R.dimen.standard_16,
                    )
                )
            }

            toolbar.setNavigationOnClickListener { popBackStackTwice() }

            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStackTwice()
        }

        viewModel.listItems.observe(viewLifecycleOwner) {
            validationResultAdapter.update(it)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun popBackStackTwice() {
        popBackStack()
        popBackStack()
    }
}
