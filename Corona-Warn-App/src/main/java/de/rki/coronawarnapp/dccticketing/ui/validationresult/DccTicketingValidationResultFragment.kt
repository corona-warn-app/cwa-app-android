package de.rki.coronawarnapp.dccticketing.ui.validationresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccTicketingValidationResultBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccTicketingValidationResultFragment : Fragment(R.layout.fragment_dcc_ticketing_validation_result), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var validationResultAdapter: ValidationResultAdapter

    private val binding: FragmentDccTicketingValidationResultBinding by viewBinding()
    private val args: DccTicketingValidationResultFragmentArgs by navArgs()
    private val resultViewModel: DccTicketingValidationResultViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccTicketingValidationResultViewModel.Factory
            factory.create(transactionContext = args.transactionContext)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            validationResultFragments.apply {
                args.transactionContext.resultTokenPayload?.result?.let { setHeaderForState(it) }
                list.apply {
                    adapter = validationResultAdapter
                }

                toolbar.setNavigationOnClickListener { resultViewModel.onCloseClicked() }

                appBarLayout.onOffsetChange { _, subtitleAlpha ->
                    headerImage.alpha = subtitleAlpha
                }
            }

            buttonDone.setOnClickListener { resultViewModel.onDoneClicked() }
        }

        resultViewModel.items.observe2(this) {
            validationResultAdapter.update(it)
        }

        resultViewModel.navigation.observe2(this) {
            handleNavigation(it)
        }
    }

    private fun handleNavigation(navigation: DccTicketingValidationNavigation) {
        when (navigation) {
            DccTicketingValidationNavigation.Back -> popBackStack()
        }
    }
}
