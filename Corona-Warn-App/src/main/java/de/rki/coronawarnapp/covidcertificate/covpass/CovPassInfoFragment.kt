package de.rki.coronawarnapp.covidcertificate.covpass

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCovPassInfoBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import setTextWithUrl

class CovPassInfoFragment : Fragment(R.layout.fragment_cov_pass_info) {

    private val binding: FragmentCovPassInfoBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            toolbar.setNavigationOnClickListener { popBackStack() }

            linkToFaq.setTextWithUrl(
                R.string.cov_pass_info_faq_link_label,
                R.string.cov_pass_info_faq_link_label,
                R.string.cov_pass_info_faq_link
            )

            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }
        }
    }
}
