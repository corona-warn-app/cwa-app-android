package de.rki.coronawarnapp.eventregistration.events.ui.category

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.EventRegistrationCategoryFragmentBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.setGone
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class TraceLocationCategoryFragment : Fragment(R.layout.event_registration_category_fragment) {

    private val binding: EventRegistrationCategoryFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categoryNavbar.headerButtonBack.buttonIcon.setOnClickListener {
            popBackStack()
        }

        traceLocationCategories
            .filter { it.uiType == TraceLocationUIType.LOCATION }
            .forEach {
                inflateCategories(it, binding.layoutLocations)
            }

        traceLocationCategories
            .filter { it.uiType == TraceLocationUIType.EVENT }
            .forEach {
                inflateCategories(it, binding.layoutEvents)
            }
    }

    private fun inflateCategories(category: TraceLocationCategory, layout: ViewGroup) {
        val categoryLayout = layoutInflater.inflate(R.layout.event_registration_category_item, null)

        val titleTextView = categoryLayout.findViewById<TextView>(R.id.title)
        val subtitleTextView = categoryLayout.findViewById<TextView>(R.id.subtitle)

        titleTextView.text = getString(category.title)

        if (category.subtitle != null) {
            subtitleTextView.text = getString(category.subtitle)
        } else {
            subtitleTextView.setGone(true)
        }

        categoryLayout.setOnClickListener {
            // TODO in next PR - continue event creation flow and pass TraceLocationCategory (it) to it
        }

        layout.addView(categoryLayout)
    }

    override fun onResume() {
        super.onResume()
        binding.categoryRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
