package de.rki.coronawarnapp.eventregistration.events.ui.category

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.EventRegistrationCategoryFragmentBinding
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.CategoryItem
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.TraceLocationCategoryAdapter
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.category.traceLocationCategories
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.header.TraceLocationHeaderItem
import de.rki.coronawarnapp.eventregistration.events.ui.category.adapter.separator.TraceLocationSeparatorItem
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import timber.log.Timber

class TraceLocationCategoryFragment : Fragment(R.layout.event_registration_category_fragment) {

    private val binding: EventRegistrationCategoryFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { popBackStack() }

        val categoryList = mutableListOf<CategoryItem>().apply {
            add(TraceLocationHeaderItem(R.string.tracelocation_organizer_category_type_location_header))
            addAll(traceLocationCategories.filter { it.uiType == TraceLocationUIType.LOCATION })
            add(TraceLocationSeparatorItem)
            add(TraceLocationHeaderItem(R.string.tracelocation_organizer_category_type_event_header))
            addAll(traceLocationCategories.filter { it.uiType == TraceLocationUIType.EVENT })
        }

        val adapter = TraceLocationCategoryAdapter(categoryList) {
            // TODO: Set click-listener - Continue with event creation flow in next PR
            Timber.d("Clicked on TraceLocationCategory: $it")
        }

        binding.recyclerViewCategories.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        binding.categoryRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
