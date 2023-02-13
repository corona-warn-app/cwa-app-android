package de.rki.coronawarnapp.statistics.ui.stateselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FederalStateSelectionFragmentBinding
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class FederalStateSelectionFragment : Fragment(R.layout.federal_state_selection_fragment), AutoInject {

    private val navArgs by navArgs<FederalStateSelectionFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: FederalStateSelectionViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as FederalStateSelectionViewModel.Factory

            val mappedFederalState = PpaData.PPAFederalState.forNumber(navArgs.selectedFederalState)
            factory.create(mappedFederalState)
        }
    )

    private val binding: FederalStateSelectionFragmentBinding by viewBinding()

    private val itemAdapter = FederalStateAdapter { viewModel.selectUserInfoItem(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.apply {
                setTitle(getToolbarLabel())
                setNavigationOnClickListener { popBackStack() }
                if (navArgs.selectedFederalState != NO_STATE_SELECTED) {
                    setNavigationIcon(R.drawable.ic_back)
                } else {
                    setNavigationIcon(R.drawable.ic_close)
                }
            }
            inputList.adapter = itemAdapter
        }

        viewModel.apply {
            listItems.observe(viewLifecycleOwner) {
                itemAdapter.data = it
            }
            event.observe(viewLifecycleOwner) {
                when (it) {
                    FederalStateSelectionViewModel.Events.FinishEvent -> findNavController().popBackStack(
                        R.id.mainFragment,
                        false
                    )
                    is FederalStateSelectionViewModel.Events.OpenDistricts -> findNavController().navigate(
                        FederalStateSelectionFragmentDirections
                            .actionFederalStateSelectionFragmentToFederalStateSelectionFragment(
                                it.selectedFederalState.number
                            )
                    )
                }
            }
        }
    }

    private fun getToolbarLabel() = if (navArgs.selectedFederalState != NO_STATE_SELECTED) {
        R.string.analytics_userinput_district_title
    } else {
        R.string.analytics_userinput_federalstate_title
    }

    companion object {
        private const val NO_STATE_SELECTED = -1
    }
}
