package de.rki.coronawarnapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainShareBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel

/**
 * This fragment informs the user about what he is going to share and how he is going to help everybody with this :)
 *
 * @see TracingViewModel
 */
class MainShareFragment : BaseFragment() {

    companion object {
        private val TAG: String? = MainShareFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private lateinit var binding: FragmentMainShareBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainShareBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.mainShareButton.setOnClickListener {
            share()
        }
        binding.mainShareHeader.informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    // TODO move to helper
    private fun share() {
        val share = Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.main_share_message))
        }, null)
        startActivity(share)
    }
}
