package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ExpirationNoticeCardLayoutBinding

class ExpirationNoticeCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val binding: ExpirationNoticeCardLayoutBinding

    init {
        LayoutInflater.from(context).inflate(R.layout.expiration_notice_card_layout, this, true)
        binding = ExpirationNoticeCardLayoutBinding.bind(this)
    }

    fun setText(text: String) {
        binding.expirationDate.text = text
    }
}
