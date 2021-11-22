package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R

class DccTicketingCertificateSelectionFragment : Fragment() {

    private val args by navArgs<DccTicketingCertificateSelectionFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // TODO Pass args.transactionContext.accessTokenPayload to the view model
        return inflater.inflate(R.layout.fragment_dcc_ticketing_certificate_selection, container, false)
    }
}
