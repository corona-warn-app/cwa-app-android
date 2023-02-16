package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FabTooltipBinding

class FabTooltipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: FabTooltipBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.fab_tooltip, this, true)
        binding = FabTooltipBinding.bind(this)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        binding.close.setOnClickListener(l)
    }
}
