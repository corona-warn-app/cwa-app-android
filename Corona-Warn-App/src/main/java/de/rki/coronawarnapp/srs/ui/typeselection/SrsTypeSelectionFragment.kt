package de.rki.coronawarnapp.srs.ui.typeselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsTypeSelectionBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SrsTypeSelectionFragment : Fragment(R.layout.fragment_srs_type_selection), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SrsTypeSelectionFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding by viewBinding<FragmentSrsTypeSelectionBinding>()

    @Inject lateinit var itemAdapter: SrsTypeSelectionItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe2(this) {
            when (it) {
                SrsTypeSelectionNavigationEvents.NavigateToCloseDialog -> showCancelDialog()
                SrsTypeSelectionNavigationEvents.NavigateToMainScreen ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections.actionSrsSubmissionTypeSelectionFragmentToMainFragment()
                    )

                is SrsTypeSelectionNavigationEvents.NavigateToShareCheckins ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections
                            .actionSrsSubmissionTypeSelectionFragmentToSrsCheckinsFragment()
                    )

                is SrsTypeSelectionNavigationEvents.NavigateToShareSymptoms ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections
                            .actionSrsSubmissionTypeSelectionFragmentToSrsSymptomsFragment()
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

    private fun showCancelDialog() {
        displayDialog {
            title(R.string.srs_cancel_dialog_title)
            message(R.string.srs_cancel_dialog_body)
            positiveButton(R.string.srs_cancel_dialog_positive_button)
            negativeButton(R.string.srs_cancel_dialog_negative_button) { viewModel.onCancelConfirmed() }
        }
    }
}
