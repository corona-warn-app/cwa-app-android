package de.rki.coronawarnapp.familytest.ui.testlist

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentFamilyTestListBinding
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class FamilyTestListFragment : Fragment(R.layout.fragment_family_test_list), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: FamilyTestListViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentFamilyTestListBinding by viewBinding()
    private val familyTestListAdapter = FamilyTestListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu(binding.toolbar)
        bindRecycler()
        binding.refreshLayout.setOnRefreshListener { viewModel.onRefreshTests() }
        binding.toolbar.setOnClickListener { viewModel.onBackPressed() }
        viewModel.familyTests.observe2(this) { tests -> updateViews(tests) }
        viewModel.events.observe2(this) { it?.let { onNavigationEvent(it) } }
        viewModel.refreshComplete.observe2(this) { binding.refreshLayout.isRefreshing = false }
    }

    private fun onNavigationEvent(event: FamilyTestListEvent) {
        when (event) {
            is FamilyTestListEvent.NavigateBack -> popBackStack()
            is FamilyTestListEvent.ConfirmSwipeTest -> showRemovalConfirmation(event.familyCoronaTest, event.position)
            is FamilyTestListEvent.ConfirmRemoveTest -> showRemovalConfirmation(event.familyCoronaTest, null)
            is FamilyTestListEvent.ConfirmRemoveAllTests -> showRemovalConfirmation(null, null)
        }
    }

    private fun updateViews(tests: List<FamilyTestListItem>) {
        familyTestListAdapter.update(tests)
    }

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_remove_all -> {
                    viewModel.onRemoveAllTests()
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun bindRecycler() {
        binding.testsList.apply {
            adapter = familyTestListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            setupSwipe(context = requireContext())
        }
    }

    private fun showRemovalConfirmation(familyCoronaTest: FamilyCoronaTest?, position: Int?) =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(
                if (familyCoronaTest == null) R.string.family_tests_list_deletion_alert_header
                else R.string.family_tests_list_deletion_alert_header
            )
            setMessage(R.string.family_tests_list_deletion_alert_body)
            setPositiveButton(R.string.family_tests_list_deletion_alert_delete_button) { _, _ ->
                viewModel.onRemoveTestConfirmed(familyCoronaTest)
            }
            setNegativeButton(R.string.family_tests_list_deletion_alert_cancel_button) { _, _ -> }
            setOnDismissListener {
                position?.let { familyTestListAdapter.notifyItemChanged(position) }
            }
        }.show()
}
