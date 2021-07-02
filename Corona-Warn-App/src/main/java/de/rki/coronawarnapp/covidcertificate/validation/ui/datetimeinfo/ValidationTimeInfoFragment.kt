package de.rki.coronawarnapp.covidcertificate.validation.ui.datetimeinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ValidationTimeInfoFragmentBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class ValidationTimeInfoFragment : Fragment(R.layout.validation_time_info_fragment) {

    private val binding: ValidationTimeInfoFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }
        }
    }
}
