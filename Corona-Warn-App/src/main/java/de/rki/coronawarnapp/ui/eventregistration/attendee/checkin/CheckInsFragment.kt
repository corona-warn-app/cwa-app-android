package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CheckInsFragment : Fragment(R.layout.fragment_check_ins), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CheckInsViewModel by cwaViewModels { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
