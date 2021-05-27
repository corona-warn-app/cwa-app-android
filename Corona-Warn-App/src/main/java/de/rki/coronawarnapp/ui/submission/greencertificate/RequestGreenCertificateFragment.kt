package de.rki.coronawarnapp.ui.submission.greencertificate

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentRequestGreenCertificateBinding
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class RequestGreenCertificateFragment : Fragment(R.layout.fragment_request_green_certificate) {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel by cwaViewModelsAssisted<RequestGreenCertificateViewModel>(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as RequestGreenCertificateViewModel.Factory
            factory.create(args.testType)
        }
    )
    private val binding by viewBinding<FragmentRequestGreenCertificateBinding>()
    private val args by navArgs<RequestGreenCertificateFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}
