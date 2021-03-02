package de.rki.coronawarnapp.ui.eventregistration.checkin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfrimCheckInBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ConfirmCheckInFragment : Fragment(R.layout.fragment_confrim_check_in), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ConfirmCheckInViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentConfrimCheckInBinding by viewBindingLazy()
    private val args by navArgs<ConfirmCheckInFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.decodeEvent(args.encodedEvent)
        viewModel.eventData.observe2(this) {
            binding.encodedEvent.text = with(it) {
                """
            guid=${String(guid.toByteArray())}
            desc=$description
            start=$start
            end=$end
            defaultCheckInLengthInMinutes=$defaultCheckInLengthInMinutes
            """.trimIndent()
            }
        }
    }
}
