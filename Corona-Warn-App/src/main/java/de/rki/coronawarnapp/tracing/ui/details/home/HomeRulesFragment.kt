package de.rki.coronawarnapp.tracing.ui.details.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTracingDetailsHomeBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import setTextWithUrl

class HomeRulesFragment : Fragment(R.layout.fragment_tracing_details_home) {

    private val binding: FragmentTracingDetailsHomeBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            faqText.setTextWithUrl(
                R.string.risk_details_minimize_risk_faq,
                R.string.risk_details_minimize_risk_faq_label,
                R.string.risk_details_minimize_risk_faq_link
            )
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }
    }
}
