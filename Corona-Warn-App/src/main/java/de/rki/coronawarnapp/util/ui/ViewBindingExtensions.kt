package de.rki.coronawarnapp.util.ui

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.MainThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <FragmentT : Fragment, reified BindingT : ViewBinding> FragmentT.viewBindingLazy() =
    viewBindingLazy {
        val bindingMethod = BindingT::class.java.getMethod("bind", View::class.java)
        val binding = bindingMethod(null, requireView()) as BindingT
        if (binding is ViewDataBinding) {
            binding.lifecycleOwner = this
        }
        binding
    }

@Suppress("unused")
fun <FragmentT : Fragment, BindingT : ViewBinding> FragmentT.viewBindingLazy(
    bindingProvider: FragmentT.() -> BindingT
) = ViewBindingProperty(bindingProvider)

class ViewBindingProperty<ComponentT : LifecycleOwner, BindingT : ViewBinding>(
    private val bindingProvider: (ComponentT) -> BindingT
) : ReadOnlyProperty<ComponentT, BindingT> {

    private val uiHandler = Handler(Looper.getMainLooper())
    private var localRef: ComponentT? = null
    private var viewBinding: BindingT? = null

    private val onDestroyObserver = object : DefaultLifecycleObserver {
        // Called right before Fragment.onDestroyView
        override fun onDestroy(owner: LifecycleOwner) {
            val ref = localRef ?: return
            ref.lifecycle.removeObserver(this)
            localRef = null
            // Otherwise the binding is null before Fragment.onDestroyView
            uiHandler.post { viewBinding = null }
        }
    }

    @MainThread
    override fun getValue(thisRef: ComponentT, property: KProperty<*>): BindingT {
        viewBinding?.let {
            // Only accessible from within the same component
            require(localRef === thisRef)
            return@getValue it
        }

        localRef = thisRef
        thisRef.lifecycle.addObserver(onDestroyObserver)

        return bindingProvider(thisRef).also { viewBinding = it }
    }
}
