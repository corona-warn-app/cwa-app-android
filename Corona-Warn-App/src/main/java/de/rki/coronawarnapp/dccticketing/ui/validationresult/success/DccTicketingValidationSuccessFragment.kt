package de.rki.coronawarnapp.dccticketing.ui.validationresult.success

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingValidationSuccessBinding
import de.rki.coronawarnapp.dccticketing.ui.validationresult.success.common.ValidationResultAdapter
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.lists.decorations.RecylerViewPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccTicketingValidationSuccessFragment : Fragment(R.layout.fragment_dcc_ticketing_validation_success) {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentDccTicketingValidationSuccessBinding by viewBinding()
    private val args: DccTicketingValidationSuccessFragmentArgs by navArgs()
    private val viewModel: DccTicketingValidationViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingValidationViewModel.Factory
            factory.create(transactionContext = args.transactionContext)
        }
    )

    @Inject lateinit var validationResultAdapter: ValidationResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            covidCertificateValidationResultFragments.apply {
                setHeaderForState(args.transactionContext.resultTokenPayload?.result)
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

                toolbar.setNavigationOnClickListener { viewModel.onCloseClicked() }

                appBarLayout.onOffsetChange { _, subtitleAlpha ->
                    headerImage.alpha = subtitleAlpha
                }
            }

            buttonDone.setOnClickListener { viewModel.onDoneClicked() }
        }

        viewModel.items.observe2(this) {
            validationResultAdapter.update(it)
        }

        viewModel.navigation.observe2(this) {
            handleNavigation(it)
        }
    }

    private fun handleNavigation(navigation: DccTicketingValidationNavigation) {
        when (navigation) {
            DccTicketingValidationNavigation.Back -> popBackStack()
        }
    }
}
