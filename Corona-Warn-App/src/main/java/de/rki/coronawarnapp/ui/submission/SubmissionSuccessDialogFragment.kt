package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionDialogBinding

/**
 * A simple [SubmissionSuccessDialogFragment] subclass.
 */
class SubmissionSuccessDialogFragment : DialogFragment() {

    companion object {
        private val TAG: String? = SubmissionSuccessDialogFragment::class.simpleName
    }

    private var _binding: FragmentSubmissionDialogBinding? = null
    private val binding: FragmentSubmissionDialogBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionDialogBinding.inflate(inflater)

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.submissionVerificationSuccessButton.setOnClickListener {
            Log.i(TAG, "button OK clicked")
            dismiss()
            findNavController().navigate(
                SubmissionSuccessDialogFragmentDirections.actionSubmissionSuccessDialogFragmentToMainFragment()
            )
        }
    }
}
