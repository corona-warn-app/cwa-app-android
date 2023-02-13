package de.rki.coronawarnapp.ui.presencetracing.organizer.category

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerCategoryFragmentBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.TraceLocationCategoryAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class TraceLocationCategoryFragment : Fragment(R.layout.trace_location_organizer_category_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TraceLocationCategoryViewModel by cwaViewModels { viewModelFactory }

    private val binding: TraceLocationOrganizerCategoryFragmentBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform()
        sharedElementReturnTransition = MaterialContainerTransform()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            setNavigationOnClickListener { popBackStack() }
            addTitleId(R.id.trace_location_organizer_category_fragment_title_id)
        }

        vm.categoryItems.observe(viewLifecycleOwner) { categoryItems ->
            val adapter = TraceLocationCategoryAdapter(categoryItems) {
                findNavController().navigate(
                    TraceLocationCategoryFragmentDirections
                        .actionTraceLocationOrganizerCategoriesFragmentToTraceLocationCreateFragment(it)
                )
            }
            binding.recyclerViewCategories.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        binding.categoryRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
