package de.rki.coronawarnapp.srs.ui.typeselection

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsTypeSelectionBinding
import de.rki.coronawarnapp.srs.ui.dialogs.showCloseDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import javax.inject.Inject

@AndroidEntryPoint
class SrsTypeSelectionFragment : Fragment(R.layout.fragment_srs_type_selection) {

    private val viewModel: SrsTypeSelectionFragmentViewModel by viewModels()
    private val binding by viewBinding<FragmentSrsTypeSelectionBinding>()
    @Inject lateinit var itemAdapter: SrsTypeSelectionItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe(viewLifecycleOwner) {
            when (it) {
                SrsTypeSelectionNavigationEvents.NavigateToCloseDialog -> showCloseDialog {
                    viewModel.onCancelConfirmed()
                }

                SrsTypeSelectionNavigationEvents.NavigateToMainScreen ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections.actionSrsSubmissionTypeSelectionFragmentToMainFragment()
                    )

                is SrsTypeSelectionNavigationEvents.NavigateToShareCheckins ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections
                            .actionSrsSubmissionTypeSelectionFragmentToSrsCheckinsFragment(it.type)
                    )

                is SrsTypeSelectionNavigationEvents.NavigateToShareSymptoms ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections
                            .actionSrsSubmissionTypeSelectionFragmentToSrsSymptomsFragment(
                                submissionType = it.type,
                                selectedCheckIns = longArrayOf()
                            )
                    )
            }
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onCancel()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        binding.typeList.adapter = itemAdapter
        viewModel.types.observe(viewLifecycleOwner) { types ->
            itemAdapter.data = types
            binding.typeSelectionNextButton.isEnabled = types.any { it.checked }
        }

        itemAdapter.onItemClickListener = { viewModel.selectTypeListItem(it) }
        binding.toolbar.setNavigationOnClickListener { viewModel.onCancel() }
        binding.typeSelectionNextButton.setOnClickListener {
            viewModel.onNextClicked()
        }
    }
}
