package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.AgeWarningLayoutBinding

class AgeWarningView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: AgeWarningLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.age_warning_layout, this, true)
        binding = AgeWarningLayoutBinding.bind(this)
    }
}
