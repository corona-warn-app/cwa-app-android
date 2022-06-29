package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.passed

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.ValidationResultAdapter
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.setHeaderForState
import de.rki.coronawarnapp.databinding.CovidCertificateValidationPassedFragmentBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.RecylerViewPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccValidationPassedFragment : Fragment(R.layout.covid_certificate_validation_passed_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: CovidCertificateValidationPassedFragmentBinding by viewBinding()
    private val args: DccValidationPassedFragmentArgs by navArgs()
    private val viewModel: DccValidationPassedViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccValidationPassedViewModel.Factory
            factory.create(validation = args.validation)
        }
    )

    @Inject lateinit var validationResultAdapter: ValidationResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            covidCertificateValidationResultFragments.apply {
                setHeaderForState(args.validation.state, args.validation.acceptanceRules.size)
                list.apply {
                    adapter = validationResultAdapter
                    val padding = R.dimen.spacing_small
                    addItemDecoration(
                        RecylerViewPaddingDecorator(
                            topPadding = padding,
                            leftPadding = padding,
                            rightPadding = padding
                        )
                    )
                }

                toolbar.setNavigationOnClickListener {
                    popBackStackTwice()
                }

                appBarLayout.onOffsetChange { _, subtitleAlpha ->
                    headerImage.alpha = subtitleAlpha
                }
            }

            checkAnotherCountryButton.setOnClickListener { popBackStack() }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            popBackStackTwice()
        }

        viewModel.items.observe2(this) {
            validationResultAdapter.update(it)
        }
    }

    private fun popBackStackTwice() {
        popBackStack()
        popBackStack()
    }
}
