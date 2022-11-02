package de.rki.coronawarnapp.srs.ui.typeselction

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsTypeSelectionBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding

class SrsTypeSelectionFragment : Fragment(R.layout.fragment_srs_type_selection), AutoInject {
    private val binding by viewBinding<FragmentSrsTypeSelectionBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
