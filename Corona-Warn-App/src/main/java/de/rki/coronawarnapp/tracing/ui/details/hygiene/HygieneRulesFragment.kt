package de.rki.coronawarnapp.tracing.ui.details.hygiene

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTracingDetailsHygieneBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class HygieneRulesFragment : Fragment(R.layout.fragment_tracing_details_hygiene) {
    private val binding: FragmentTracingDetailsHygieneBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }
    }
}
