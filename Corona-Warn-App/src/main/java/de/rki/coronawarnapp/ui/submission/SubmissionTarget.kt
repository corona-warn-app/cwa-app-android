package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTargetBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import kotlinx.android.synthetic.main.include_target.view.*

/**
 * Submission interoperability question screen.
 */
class SubmissionTarget : Fragment() {

    private var _binding: FragmentSubmissionTargetBinding? = null
    private val binding: FragmentSubmissionTargetBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionTargetBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.target.targetVerify.setOnClickListener {
            onClickButtonVerifyHandler()
        }

        binding.submissionTargetHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.target.targetButtonApply.setOnClickListener {
            onClickButtonApplyHandler()
        }

        binding.target.targetButtonReject.setOnClickListener {
            onClickButtonRejectHandler()
        }
    }

    // onClickListener for targetButtonVerify. Change style and enabled state for next Button

    private fun onClickButtonVerifyHandler() {
        val constraintLayout = binding.target.targetButtonVerify
        val textView = binding.target.targetButtonVerify.target_verify
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.target.targetButtonApply, binding.target.targetButtonReject)
        val initStateTextView: Array<TextView> = arrayOf(
            binding.target.targetButtonApply.target_apply,
            binding.target.targetButtonReject.target_reject
        )
        val buttonNext = binding.submissionTargetButtonNext

        changeState(constraintLayout, textView, initStateLayout, initStateTextView, buttonNext)
    }

    // onClickListener for targetButtonApply. Change style and enabled state for next Button

    private fun onClickButtonApplyHandler() {
        val constraintLayout = binding.target.targetButtonApply
        val textView = binding.target.targetButtonApply.target_apply
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.target.targetButtonReject, binding.target.targetButtonVerify)
        val initStateTextView: Array<TextView> = arrayOf(
            binding.target.targetButtonReject.target_reject,
            binding.target.targetButtonVerify.target_verify
        )
        val buttonNext = binding.submissionTargetButtonNext

        changeState(constraintLayout, textView, initStateLayout, initStateTextView, buttonNext)
    }

    // onClickListener for targetButtonReject. Change style and enabled state for next Button

    private fun onClickButtonRejectHandler() {
        val constraintLayout = binding.target.targetButtonReject
        val textView = binding.target.targetButtonReject.target_reject
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.target.targetButtonApply, binding.target.targetButtonVerify)
        val initStateTextView: Array<TextView> = arrayOf(
            binding.target.targetButtonApply.target_apply,
            binding.target.targetButtonVerify.target_verify
        )
        val buttonNext = binding.submissionTargetButtonNext

        changeState(constraintLayout, textView, initStateLayout, initStateTextView, buttonNext)
    }

    // change button's state - background and text color, when button is pressed or not, and enabled state for next button

    fun changeState(
        constraintLayout: ConstraintLayout,
        textView: TextView,
        initStateLayout: Array<ConstraintLayout>,
        initStateText: Array<TextView>,
        buttonNext: Button
    ) {
        val context = constraintLayout.context
        var color = context.getColorStateList(R.color.colorGreenButtonNotPressed)
        var textColor = context.getColor(R.color.colorTextGreenButtonNotPressed)
        var enabledNext = false

        if (constraintLayout.backgroundTintList === color) {
            color = context.getColorStateList(R.color.colorGreenButtonPressed)
            textColor = context.getColor(R.color.colorTextGreenButtonPressed)
            initStateLayout.forEach { entry ->
                entry.backgroundTintList =
                    context.getColorStateList(R.color.colorGreenButtonNotPressed)
            }
            initStateText.forEach { entry ->
                entry.setTextColor(context.getColor(R.color.colorTextGreenButtonNotPressed))
            }
            enabledNext = true
        }

        buttonNext.isEnabled = enabledNext
        constraintLayout.backgroundTintList = color
        textView.setTextColor(textColor)
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTargetContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
