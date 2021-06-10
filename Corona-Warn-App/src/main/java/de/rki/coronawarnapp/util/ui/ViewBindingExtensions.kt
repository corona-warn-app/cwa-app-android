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
import com.google.android.material.bottomnavigation.BottomNavigationView

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
            Timber.tag(TAG).v("onDestroy(%s)", owner)
            localRef?.lifecycle?.removeObserver(this) ?: return

            localRef = null

            uiHandler.post {
                Timber.tag(TAG).v("Resetting viewBinding owner=%s", owner)
                viewBinding = null
            }
        }
    }

    @MainThread
    override fun getValue(thisRef: ComponentT, property: KProperty<*>): BindingT {
        if (localRef == null && viewBinding != null) {
            Timber.tag(TAG).w(
                "Fragment.onDestroyView(%s) was called, but the handler didn't execute our delayed reset.",
                thisRef
            )
            /**
             * There is a fragment race condition if you navigate to another fragment and quickly popBackStack().
             * Our uiHandler.post { } will not have executed for some reason.
             * In that case we manually null the old viewBinding, to allow for clean recreation.
             */
            viewBinding = null
        }

        /**
         * This is a fix for an edge case where the fragment is created but was never visible to the user
         * [DefaultLifecycleObserver.onDestroy] was never called despite that the [Fragment.onDestroyView] was called,
         * therefore the ViewBinding will not be set to `null` and will hold the old view ,while Fragment will
         * inflate a new [View] when navigating back to it. As result of that the screen ends being blank.
         *
         * This is very specific case when navigating by deeplink to one of the [BottomNavigationView] destinations,
         * that is not the "home destination" of the graph.
         */
        (localRef as? Fragment)?.view?.let {
            if (it != viewBinding?.root && localRef === thisRef) {
                Timber.tag(TAG).w("Different view for the same fragment, resetting old viewBinding")
                viewBinding = null
            }
        }

        viewBinding?.let {
            // Only accessible from within the same component
            require(localRef === thisRef)
            return@getValue it
        }

        val lifecycle = lifecycleOwnerProvider(thisRef).lifecycle

        return bindingProvider(thisRef).also {
            Timber.tag(TAG).d("bindingProvider(%s)", thisRef)
            viewBinding = it
            localRef = thisRef
            lifecycle.addObserver(onDestroyObserver)
        }
    }
}

private const val TAG = "ViewBindingExtension"
