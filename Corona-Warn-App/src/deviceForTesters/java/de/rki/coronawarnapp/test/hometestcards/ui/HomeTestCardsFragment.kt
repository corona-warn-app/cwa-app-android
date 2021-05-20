package de.rki.coronawarnapp.test.hometestcards.ui

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestHomeTestCardsLayoutBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class HomeTestCardsFragment : Fragment(R.layout.fragment_test_home_test_cards_layout), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: HomeTestCardsFragmentViewModel by cwaViewModels { viewModelFactory }

    val binding: FragmentTestHomeTestCardsLayoutBinding by viewBinding()

    private val homeAdapter = HomeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = homeAdapter
        }

        viewModel.homeItems.observe2(this) {
            homeAdapter.update(it)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Home Cards",
            description = "View all possible test result cards (PCR and Antigen).",
            targetId = R.id.homeTestCardsFragment
        )
    }
}
