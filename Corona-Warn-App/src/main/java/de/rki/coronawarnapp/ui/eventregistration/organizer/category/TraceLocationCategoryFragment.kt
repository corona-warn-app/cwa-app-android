package de.rki.coronawarnapp.ui.eventregistration.organizer.category

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerCategoryFragmentBinding
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.TraceLocationCategoryAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class TraceLocationCategoryFragment : Fragment(R.layout.trace_location_organizer_category_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TraceLocationCategoryViewModel by cwaViewModels { viewModelFactory }

    private val binding: TraceLocationOrganizerCategoryFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        vm.categoryItems.observe2(this) { categoryItems ->
            val adapter = TraceLocationCategoryAdapter(categoryItems) {
                // TODO: Set click-listener - Continue with event creation flow in next PR
                Timber.d("Clicked on TraceLocationCategory: $it")
            }
            binding.recyclerViewCategories.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        binding.categoryRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
