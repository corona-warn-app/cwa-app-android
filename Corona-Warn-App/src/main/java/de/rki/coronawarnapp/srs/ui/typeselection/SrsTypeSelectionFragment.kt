package de.rki.coronawarnapp.srs.ui.typeselection

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsTypeSelectionBinding
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatSrsTypeSelectionBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatSrsTypeSelectionButtonTextStyleByState
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
                SrsTypeSelectionNavigationEvents.NavigateToShareCheckins ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections
                            .actionSrsSubmissionTypeSelectionFragmentToSrsCheckinsFragment()
                    )
                SrsTypeSelectionNavigationEvents.NavigateToShareSymptoms ->
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

        binding.typeList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }

        itemAdapter.data = typeList

        itemAdapter.onItemClickListener = {
            viewModel.selectTypeListItem(it)
        }

        binding.toolbar.setNavigationOnClickListener { viewModel.onCancel() }

        viewModel.srsTestType.observe2(this) {
            updateButtons(it)
        }
    }

    private fun updateButtons(typeSelectionItem: SrsTypeSelectionItem) {

        binding.typeList.children.forEachIndexed { i, view ->
            (view as Button).apply {
                handleColors(typeSelectionItem.submissionType, typeList[i].submissionType)
            }
        }

        binding.typeSelectionNextButton.apply {
            isEnabled = typeSelectionItem.submissionType != null
            setOnClickListener { viewModel.onNextClicked() }
        }
    }

    private fun Button.handleColors(submissionType: SrsSubmissionType?, state: SrsSubmissionType?) {
        setTextColor(formatSrsTypeSelectionButtonTextStyleByState(context, submissionType, state))
        backgroundTintList =
            ColorStateList.valueOf(formatSrsTypeSelectionBackgroundButtonStyleByState(context, submissionType, state))
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
