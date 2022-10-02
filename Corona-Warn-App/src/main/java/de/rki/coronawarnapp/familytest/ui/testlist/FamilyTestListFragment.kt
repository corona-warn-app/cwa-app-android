package de.rki.coronawarnapp.familytest.ui.testlist

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.databinding.FragmentFamilyTestListBinding
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
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
        binding.toolbar.setNavigationOnClickListener { viewModel.onBackPressed() }
        viewModel.familyTests.observe2(this) { tests -> updateViews(tests) }
        viewModel.events.observe2(this) { it?.let { onNavigationEvent(it) } }
        viewModel.error.observe2(this) { it.toErrorDialogBuilder(requireContext()).show() }
        viewModel.refreshComplete.observe2(this) { binding.refreshLayout.isRefreshing = false }
    }

    override fun onStop() {
        super.onStop()
        viewModel.markAllTestAsViewed()
    }

    private fun onNavigationEvent(event: FamilyTestListEvent) {
        when (event) {
            is FamilyTestListEvent.NavigateBack -> popBackStack()
            is FamilyTestListEvent.ConfirmSwipeTest -> showRemovalConfirmation(event.familyCoronaTest, event.position)
            is FamilyTestListEvent.ConfirmRemoveTest -> showRemovalConfirmation(event.familyCoronaTest, null)
            is FamilyTestListEvent.DeleteTest -> viewModel.deleteTest(event.familyCoronaTest)
            is FamilyTestListEvent.ConfirmRemoveAllTests -> showRemovalConfirmation(null, null)
            is FamilyTestListEvent.NavigateToDetails -> openDetailsScreen(event.familyCoronaTest)
        }
    }

    private fun openDetailsScreen(familyCoronaTest: FamilyCoronaTest) {
        val coronaTest = familyCoronaTest.coronaTest
        when (coronaTest.state) {
            CoronaTest.State.PENDING -> doNavigate(
                FamilyTestListFragmentDirections.actionFamilyTestListFragmentToPendingTestResult(
                    testIdentifier = coronaTest.identifier
                )
            )
            CoronaTest.State.INVALID -> doNavigate(
                FamilyTestListFragmentDirections.actionFamilyTestListFragmentToSubmissionTestResultInvalidFragment(
                    testIdentifier = coronaTest.identifier
                )
            )
            CoronaTest.State.POSITIVE -> doNavigate(
                FamilyTestListFragmentDirections.actionUniversalScannerToSubmissionTestResultKeysSharedFragment(
                    testIdentifier = coronaTest.identifier
                )
            )
            CoronaTest.State.NEGATIVE -> doNavigate(
                FamilyTestListFragmentDirections.actionFamilyTestListFragmentToSubmissionTestResultNegativeFragment(
                    testIdentifier = coronaTest.identifier
                )
            )
            CoronaTest.State.REDEEMED,
            CoronaTest.State.OUTDATED,
            CoronaTest.State.RECYCLED -> Unit
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
                else -> false
            }
        }
    }

    private fun bindRecycler() {
        binding.testsList.apply {
            adapter = familyTestListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_normal))
            setupSwipe(context = requireContext())
        }
    }

    private fun showRemovalConfirmation(familyCoronaTest: FamilyCoronaTest?, position: Int?) {

        val (title, message, button) = if (familyCoronaTest == null) {
            Triple(
                R.string.family_tests_list_deletion_alert_header_all,
                R.string.family_tests_list_deletion_alert_body_all,
                R.string.family_tests_list_deletion_alert_delete_button_all
            )
        } else {
            Triple(
                R.string.family_tests_list_deletion_alert_header_single,
                R.string.family_tests_list_deletion_alert_body_single,
                R.string.family_tests_list_deletion_alert_delete_button
            )
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(button) { _, _ -> viewModel.onRemoveTestConfirmed(familyCoronaTest) }
            .setNegativeButton(R.string.family_tests_list_deletion_alert_cancel_button) { _, _ -> }
            .setOnDismissListener {
                position?.let { familyTestListAdapter.notifyItemChanged(position) }
            }.show()
    }
}
