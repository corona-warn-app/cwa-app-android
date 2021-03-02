package de.rki.coronawarnapp.ui.eventregistration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentConfrimCheckInBinding
import de.rki.coronawarnapp.eventregistration.common.decodeBase32
import de.rki.coronawarnapp.server.protocols.internal.evreg.EventOuterClass
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import timber.log.Timber

class ConfirmCheckInFragment : Fragment(R.layout.fragment_confrim_check_in) {

    private val binding: FragmentConfrimCheckInBinding by viewBindingLazy()
    private val args by navArgs<ConfirmCheckInFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.i("ConfirmCheckInFragment")

        val event = args.event ?: return
        val decodeBase32 = event.split(".")[0].decodeBase32()

        val parsedEvent = EventOuterClass.Event.parseFrom(decodeBase32.toByteArray())

        binding.encodedEvent.text = with(parsedEvent) {
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
