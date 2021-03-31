package de.rki.coronawarnapp.ui.eventregistration.organizer.poster

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.QrCodePosterFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class QrCodePosterFragment : Fragment(R.layout.qr_code_poster_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: QrCodePosterViewModel by cwaViewModels { viewModelFactory }
    private val binding: QrCodePosterFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }
}
