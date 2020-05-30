package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionPositiveOtherWarningBinding
import de.rki.coronawarnapp.ui.BaseFragment

class SubmissionResultPositiveOtherWarningFragment : BaseFragment() {

    companion object {
        private val TAG: String? = SubmissionResultPositiveOtherWarningFragment::class.simpleName
    }

    private lateinit var binding: FragmentSubmissionPositiveOtherWarningBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionPositiveOtherWarningBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.submissionPositiveOtherWarningButton.setOnClickListener {
            Log.i(TAG, "Weiter pressed")
            showShareIDConfirmationDialog()
        }
        binding.submissionPositiveOtherWarningHeader
            .informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
                Log.i(TAG, "Back button pressed")
            }
    }

    private fun showShareIDConfirmationDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.submission_positive_dialog_confirmation_title)
                setMessage(R.string.submission_positive_dialog_confirmation_body)
                setPositiveButton(
                    R.string.submission_positive_dialog_confirmation_positive
                ) { _, _ ->
                    Log.i(TAG, "Agreed")
                    // Navigate to next screen
                }
                setNegativeButton(
                    R.string.submission_positive_dialog_confirmation_negative
                ) { _, _ ->
                    Log.i(TAG, "Declined")
                }
            }
            builder.create()
        }
        alertDialog.show()
    }
}
