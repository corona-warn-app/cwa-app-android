package de.rki.coronawarnapp.covidcertificate.covpass

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCovPassInfoBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import setTextWithUrl
import javax.inject.Inject

class CovPassInfoFragment : Fragment(R.layout.fragment_cov_pass_info), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val binding: FragmentCovPassInfoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            linkToFaq.setTextWithUrl(
                R.string.cov_pass_info_faq_link,
                R.string.cov_pass_info_faq_link,
                R.string.vaccination_card_booster_eligible_faq_link //TBD
            )
        }
    }
}
