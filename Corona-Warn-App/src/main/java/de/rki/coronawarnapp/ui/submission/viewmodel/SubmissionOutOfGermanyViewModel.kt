package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class SubmissionOutOfGermanyViewModel : ViewModel() {

    private val _buttonClicked = MutableLiveData<ButtonClicked>(ButtonClicked.NONE)

    val yesAnswerActive: LiveData<Boolean> = Transformations.map(_buttonClicked) { it == ButtonClicked.YES}
    val noAnswerActive: LiveData<Boolean> = Transformations.map(_buttonClicked) { it == ButtonClicked.NO}
    val notSpecifiedActive: LiveData<Boolean> = Transformations.map(_buttonClicked) { it == ButtonClicked.NOT_SPECIFIED}
    val nextActive: LiveData<Boolean> = Transformations.map(_buttonClicked) { it != ButtonClicked.NONE}

    fun buttonClicked(buttonClicked: ButtonClicked) {
        _buttonClicked.value = buttonClicked
    }
}

enum class ButtonClicked {
    NONE,
    YES,
    NO,
    NOT_SPECIFIED
}





