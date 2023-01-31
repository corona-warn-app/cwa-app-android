package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DividerLayoutBinding

class DividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: DividerLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.divider_layout, this, true)
        binding = DividerLayoutBinding.bind(this)
    }
}
