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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import kotlinx.android.synthetic.main.fragment_submission_symptom_intro.view.*

class SubmissionSymptomIntroductionFragment : Fragment() {

    private var _binding: FragmentSubmissionSymptomIntroBinding? = null
    private val binding: FragmentSubmissionSymptomIntroBinding get() = _binding!!
    private val submissionViewModel: SubmissionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionSymptomIntroBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        binding.symptomChoiceSelection.verifyState = "verify"
        binding.symptomChoiceSelection.applyState = "apply"
        binding.symptomChoiceSelection.rejectState = "reject"
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        submissionViewModel.symptom_routeToScreen.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SymptomIntroductionEvent.NavigateToNext -> navigateToSymptomCalendar()
                is SymptomIntroductionEvent.NavigateToPreviousScreen -> navigateToMainFragment()
            }
        })
    }

    private fun navigateToSymptomCalendar() {
        // TODO: Place here the route to the next fragment
    }

    private fun navigateToMainFragment() {
        (activity as MainActivity).goBack()
    }

    private fun setButtonOnClickListener() {
        /*binding
            .settingsEuropeanFederalGatewayServerConsentRow.settingsSwitchRowSwitch
            .setOnCheckedChangeListener { switch, isEnabled ->
                if (switch.tag != IGNORE_CHANGE_TAG) {
                    submissionViewModel.updateSwitch(isEnabled)
                }
            }

        binding
            .submissionEuropeanFederalGatewayServerConsentHeader.headerButtonBack.buttonIcon
            .setOnClickListener { submissionViewModel.onBackButtonClick() }

        binding
            .submissionEuropeanFederalGatewayServerConsentButtonNext
            .setOnClickListener { submissionViewModel.onNextButtonClick() }

        binding.submissionSymptomHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }*/


        binding.symptomChoiceSelection.targetButtonVerify.setOnClickListener {
            onClickButtonVerifyHandler()
        }

        binding.symptomChoiceSelection.targetButtonApply.setOnClickListener {
            onClickButtonApplyHandler()
        }

        binding.symptomChoiceSelection.targetButtonReject.setOnClickListener {
            onClickButtonRejectHandler()
        }

    }

    private fun onChangeCurrentButtonSelected(state: String?) {
        if (submissionViewModel.currentButtonSelected.value.toString() !== state) {
            submissionViewModel.setCurrentButtonSelected(state.toString())
        } else {
            submissionViewModel.setCurrentButtonSelected("")
        }
    }

    private fun onClickButtonVerifyHandler() =
        onChangeCurrentButtonSelected(binding.symptomChoiceSelection.verifyState)

    private fun onClickButtonApplyHandler() =
        onChangeCurrentButtonSelected(binding.symptomChoiceSelection.applyState)

    private fun onClickButtonRejectHandler() =
        onChangeCurrentButtonSelected(binding.symptomChoiceSelection.rejectState)




    /* override fun onCreateView(
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
         binding.submissionSymptomInitialHeadline.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
     }*/
}
