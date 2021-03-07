package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.Hold
import com.google.android.play.core.internal.by
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCheckInsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class CheckInsFragment : Fragment(R.layout.fragment_check_ins), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CheckInsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentCheckInsBinding by viewBindingLazy()

    // Encoded event is a one-time use data and then cleared
    private val encodedEvent: String?
        get() = CheckInsFragmentArgs
            .fromBundle(requireArguments())
            .encodedEvent
            .also {
                requireArguments().clear()
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.scanCheckinQrcodeFab) {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_checkInsFragment_to_scanCheckInQrCodeFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(this to transitionName)
                )
            }
        }

        encodedEvent?.let {
            Timber.i("onViewCreated")
            viewModel.verifyEvent(it)
        }

        viewModel.navigationData.observe2(this) {
            doNavigate(
                CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragment(
                    it.toVerifiedEvent()
                )
            )
        }
    }
}
