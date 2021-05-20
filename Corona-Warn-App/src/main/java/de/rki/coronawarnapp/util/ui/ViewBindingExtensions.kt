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
import timber.log.Timber
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified BindingT : ViewBinding> Fragment.viewBinding() =
    viewBinding(
        bindingProvider = {
            val bindingMethod = BindingT::class.java.getMethod("bind", View::class.java)
            val binding = bindingMethod(null, requireView()) as BindingT
            if (binding is ViewDataBinding) {
                binding.lifecycleOwner = this.viewLifecycleOwner
            }
            binding
        },
        lifecycleOwnerProvider = { viewLifecycleOwner }
    )

@Suppress("unused")
fun <BindingT : ViewBinding> Fragment.viewBinding(
    bindingProvider: Fragment.() -> BindingT,
    lifecycleOwnerProvider: Fragment.() -> LifecycleOwner
) = ViewBindingProperty(bindingProvider, lifecycleOwnerProvider)

class ViewBindingProperty<ComponentT : LifecycleOwner, BindingT : ViewBinding>(
    private val bindingProvider: (ComponentT) -> BindingT,
    private val lifecycleOwnerProvider: ComponentT.() -> LifecycleOwner
) : ReadOnlyProperty<ComponentT, BindingT> {

    private val uiHandler = Handler(Looper.getMainLooper())
    private var localRef: ComponentT? = null
    private var viewBinding: BindingT? = null

    private val onDestroyObserver = object : DefaultLifecycleObserver {
        // Called right before Fragment.onDestroyView
        override fun onDestroy(owner: LifecycleOwner) {
            localRef?.lifecycle?.removeObserver(this) ?: return

            localRef = null

            uiHandler.post {
                Timber.v("Resetting viewBinding")
                viewBinding = null
            }
        }
    }

    @MainThread
    override fun getValue(thisRef: ComponentT, property: KProperty<*>): BindingT {
        if (localRef == null && viewBinding != null) {
            Timber.w("Fragment.onDestroyView() was called, but the handler didn't execute our delayed reset.")
            /**
             * There is a fragment racecondition if you navigate to another fragment and quickly popBackStack().
             * Our uiHandler.post { } will not have executed for some reason.
             * In that case we manually null the old viewBinding, to allow for clean recreation.
             */
            viewBinding = null
        }

        viewBinding?.let {
            // Only accessible from within the same component
            require(localRef === thisRef)
            return@getValue it
        }

        val lifecycle = lifecycleOwnerProvider(thisRef).lifecycle

        return bindingProvider(thisRef).also {
            viewBinding = it
            localRef = thisRef
            lifecycle.addObserver(onDestroyObserver)
        }
    }
}
