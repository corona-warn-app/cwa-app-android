package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel

class DccTicketingCertificateSelectionFragment : Fragment() {

    private val dccTicketingSharedViewModel:
        DccTicketingSharedViewModel by navGraphViewModels(R.id.dcc_ticketing_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // TODO Use transactionContext of shared view model
        return inflater.inflate(R.layout.fragment_dcc_ticketing_certificate_selection, container, false)
    }
}
