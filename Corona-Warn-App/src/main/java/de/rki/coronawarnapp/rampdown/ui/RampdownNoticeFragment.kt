package de.rki.coronawarnapp.rampdown.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentRampdownNoticeBinding
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding

class RampdownNoticeFragment : Fragment(R.layout.fragment_rampdown_notice), AutoInject {

    private val binding: FragmentRampdownNoticeBinding by viewBinding()
    private val navArgs by navArgs<RampdownNoticeFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = navArgs.StatusTabNotice

        binding.apply {
            toolbar.title = data.titleText
            rampdownNoticeSubtitle.text = data.subTitleText
            rampdownNoticeLongtext.text = data.longText
            data.faqAnchor?.let {
                rampdownNoticeFaqanchor.convertToHyperlink(it)
                rampdownNoticeFaqanchor.isVisible = true
            }
        }
    }
}
