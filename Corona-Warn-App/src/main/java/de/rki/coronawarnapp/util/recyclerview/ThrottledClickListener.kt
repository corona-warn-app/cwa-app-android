package de.rki.coronawarnapp.util.recyclerview

import android.view.View

/*  Prevents a recyclerview item to be double selected when double clicking on it  */
class ThrottledClickListener(
    private val interval: Long,
    private val listenerBlock: (View) -> Unit
) : View.OnClickListener {

    private var lastClickTime = 0L

    override fun onClick(v: View) {

        val time = System.currentTimeMillis()

        if (time - lastClickTime >= interval) {
            lastClickTime = time
            listenerBlock(v)
        }
    }
}
