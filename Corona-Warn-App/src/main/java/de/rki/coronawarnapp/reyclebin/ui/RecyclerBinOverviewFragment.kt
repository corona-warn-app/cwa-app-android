package de.rki.coronawarnapp.reyclebin.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.RecyclerBinOverviewFragmentBinding
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class RecyclerBinOverviewFragment : Fragment(R.layout.recycler_bin_overview_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: RecyclerBinOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: RecyclerBinOverviewFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerBinAdapter = RecyclerBinAdapter()

        with(binding) {
            toolbar.setNavigationOnClickListener { popBackStack() }
            recyclerBinList.apply {
                adapter = recyclerBinAdapter
                setupSwipe(context = requireContext())
            }
        }

        viewModel.listItems.observe2(this) {
            binding.emptyListInfoContainer.isVisible = it.isEmpty()
            recyclerBinAdapter.update(it)
        }

        viewModel.events.observe2(this) {
            when (it) {
                RecyclerBinEvent.ConfirmRemoveAll -> {
                    // TODO implement dialogs
                }
                is RecyclerBinEvent.ConfirmRemoveItem -> {
                    // TODO implement dialogs
                    if (it.position != null) {
                        recyclerBinAdapter.notifyItemChanged(it.position)
                    }
                }
                is RecyclerBinEvent.ConfirmRestoreItem -> {
                    // TODO implement dialogs
                }
            }
        }
    }
}
