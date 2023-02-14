package de.rki.coronawarnapp.ui.presencetracing.organizer.category

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerCategoryFragmentBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.TraceLocationCategoryAdapter
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class TraceLocationCategoryFragment : Fragment(R.layout.trace_location_organizer_category_fragment) {

    private val vm: TraceLocationCategoryViewModel by viewModels()
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
