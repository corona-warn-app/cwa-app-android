package de.rki.coronawarnapp.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.databinding.FragmentRegisterDeviceBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

class RegisterDeviceFragment : BaseFragment() {
    private val viewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentRegisterDeviceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterDeviceBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registrationState.observe(viewLifecycleOwner, Observer {
            if (ApiRequestState.SUCCESS == it) {
                doNavigate(
                    RegisterDeviceFragmentDirections.actionDeviceRegistrationFragmentToSubmissionResultFragment()
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.doDeviceRegistration()
    }
}
