package de.rki.coronawarnapp.reyclebin.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RecyclerBinOverviewFragmentBinding
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinAdapter
import de.rki.coronawarnapp.reyclebin.ui.dialog.removeAllItemsDialog
import de.rki.coronawarnapp.reyclebin.ui.dialog.restoreCertificateDialog
import de.rki.coronawarnapp.reyclebin.ui.dialog.restoreTestDialog
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class RecyclerBinOverviewFragment : Fragment(R.layout.recycler_bin_overview_fragment) {

    private val viewModel: RecyclerBinOverviewViewModel by viewModels()
    private val binding: RecyclerBinOverviewFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerBinAdapter = RecyclerBinAdapter()

        with(binding) {
            toolbar.apply {
                setNavigationOnClickListener { popBackStack() }
                setOnMenuItemClickListener { onMenuItemClicked(it) }
            }

            recyclerBinList.apply {
                adapter = recyclerBinAdapter
                setupSwipe(context = requireContext())
            }
        }

        viewModel.listItems.observe2(this) {
            binding.emptyListInfoContainer.isVisible = it.isEmpty()
            recyclerBinAdapter.update(it)
            binding.toolbar.menu.findItem(R.id.menu_remove_all)?.isEnabled = it.isNotEmpty()
        }

        viewModel.events.observe2(this) { handleRecyclerEvent(it) }
    }

    private fun handleRecyclerEvent(event: RecyclerBinEvent): Unit = when (event) {
        RecyclerBinEvent.ConfirmRemoveAll -> removeAllItemsDialog { viewModel.onRemoveAllItemsConfirmation() }

        is RecyclerBinEvent.RemoveCertificate -> viewModel.onRemoveCertificate(event.certificate)

        is RecyclerBinEvent.ConfirmRestoreCertificate -> restoreCertificateDialog {
            viewModel.onRestoreCertificateConfirmation(event.certificate)
        }

        is RecyclerBinEvent.ConfirmRestoreTest -> restoreTestDialog { viewModel.onRestoreTestConfirmation(event.test) }

        is RecyclerBinEvent.RemoveTest -> viewModel.onRemoveTest(event.test)
        is RecyclerBinEvent.RestoreDuplicateTest -> findNavController().navigate(
            RecyclerBinOverviewFragmentDirections.actionRecyclerBinOverviewFragmentToSubmissionDeletionWarningFragment(
                event.restoreRecycledTestRequest
            )
        )
    }

    private fun onMenuItemClicked(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_remove_all -> {
            viewModel.onRemoveAllItemsClicked()
            true
        }

        else -> false
    }
}
