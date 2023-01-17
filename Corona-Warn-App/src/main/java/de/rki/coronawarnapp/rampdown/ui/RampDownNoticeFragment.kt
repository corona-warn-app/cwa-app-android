package de.rki.coronawarnapp.rampdown.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentRampdownNoticeBinding
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class RampDownNoticeFragment : Fragment(R.layout.fragment_rampdown_notice) {

    private val binding: FragmentRampdownNoticeBinding by viewBinding()
    private val navArgs by navArgs<RampDownNoticeFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = navArgs.rampDownNotice

        binding.apply {
            toolbar.title = data.title
            toolbar.setNavigationOnClickListener { popBackStack() }
            rampDownNoticeSubtitle.text = data.subtitle
            rampDownNoticeLongtext.text = data.description
            data.faqUrl?.let {
                rampDownNoticeFaqanchor.convertToHyperlink(it)
                rampDownNoticeFaqanchor.isVisible = true
            }
        }
    }
}
