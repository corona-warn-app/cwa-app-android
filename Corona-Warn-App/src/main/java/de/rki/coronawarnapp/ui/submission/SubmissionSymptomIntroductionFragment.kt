package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_submission_symptom_intro.view.*

class SubmissionSymptomIntroductionFragment : Fragment() {

    private var _binding: FragmentSubmissionSymptomIntroBinding? = null
    private val binding: FragmentSubmissionSymptomIntroBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionSymptomIntroBinding.inflate(inflater)
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
        binding.submissionSymptomHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }

        binding.symptomButtonApply.setOnClickListener {
            onClickButtonApplyHandler()
        }

        binding.symptomButtonReject.setOnClickListener {
            onClickButtonRejectHandler()
        }

        binding.symptomButtonVerify.setOnClickListener {
            onClickButtonVerifyHandler()
        }
    }

    // onClickListener for symptomButtonVerify. Change style and enabled state for next Button
    private fun onClickButtonVerifyHandler() {
        val constraintLayout = binding.symptomButtonVerify
        val textView = binding.symptomButtonVerify.symptom_verify

        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.symptomButtonApply, binding.symptomButtonReject)

        val initStateTextView: Array<TextView> = arrayOf(
            binding.symptomButtonApply.symptom_apply,
            binding.symptomButtonReject.symptom_reject
        )

        val buttonNext = binding.symptomButtonNext

        changeState(constraintLayout, textView, initStateLayout, initStateTextView, buttonNext)
    }

    // onClickListener for symptomButtonApply. Change style and enabled state for next Button
    private fun onClickButtonApplyHandler() {
        val constraintLayout = binding.symptomButtonApply
        val textView = binding.symptomButtonApply.symptom_apply

        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.symptomButtonReject, binding.symptomButtonVerify)

        val initStateTextView: Array<TextView> = arrayOf(
            binding.symptomButtonReject.symptom_reject,
            binding.symptomButtonVerify.symptom_verify
        )
        val buttonNext = binding.symptomButtonNext

        changeState(constraintLayout, textView, initStateLayout, initStateTextView, buttonNext)
    }

    // onClickListener for sympomButtonReject. Change style and enabled state for next Button
    private fun onClickButtonRejectHandler() {
        val constraintLayout = binding.symptomButtonReject
        val textView = binding.symptomButtonReject.symptom_reject
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.symptomButtonApply, binding.symptomButtonVerify)
        val initStateTextView: Array<TextView> = arrayOf(
            binding.symptomButtonApply.symptom_apply,
            binding.symptomButtonVerify.symptom_verify
        )
        val buttonNext = binding.symptomButtonNext

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
        // binding.submissionTargetContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
