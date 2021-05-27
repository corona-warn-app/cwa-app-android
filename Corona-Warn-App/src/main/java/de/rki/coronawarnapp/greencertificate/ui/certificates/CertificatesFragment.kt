package de.rki.coronawarnapp.greencertificate.ui.certificates

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCertificatesBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CertificatesFragment : Fragment(R.layout.fragment_certificates), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModels<CertificatesViewModel> { viewModelFactory }
    private val binding by viewBinding<FragmentCertificatesBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO
    }
}
