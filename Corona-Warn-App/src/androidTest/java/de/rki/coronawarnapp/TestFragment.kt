package de.rki.coronawarnapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Test fragment with test view, [TextView] required for view lifecycle owner.
 *
 * @see [Fragment.getViewLifecycleOwner]
 */
class TestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TextView(this.context)
    }
}
