package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.materialswitch.MaterialSwitch
import de.rki.coronawarnapp.R

class SwitchRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val switch by lazy { findViewById<MaterialSwitch>(R.id.switch_view) }
    private val title by lazy { findViewById<TextView>(R.id.switch_title) }
    private val subtitleView by lazy { findViewById<TextView>(R.id.switch_subtitle) }

    init {
        LayoutInflater.from(context).inflate(R.layout.switch_row, this, true)
    }

    fun setChecked(turnedOn: Boolean?) {
        switch.isChecked = turnedOn ?: false
    }

    fun setTitle(text: String?) {
        title.text = text
    }

    fun setSubtitle(text: String?) {
        subtitleView.text = text
    }

    fun setSwitchEnabled(isEnabled: Boolean?) {
        switch.isEnabled = isEnabled ?: false
    }

    val isChecked: Boolean
        get() = switch.isChecked
}
