package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.databinding.FragmentSubmissionRegisterDeviceBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

class SubmissionRegisterDeviceFragment : BaseFragment() {
    private val viewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentSubmissionRegisterDeviceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionRegisterDeviceBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registrationState.observe(viewLifecycleOwner, Observer {
            if (ApiRequestState.SUCCESS == it) {
                doNavigate(
                    SubmissionRegisterDeviceFragmentDirections
                        .actionSubmissionRegisterDeviceFragmentToSubmissionResultFragment()
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.doDeviceRegistration()
    }
}
