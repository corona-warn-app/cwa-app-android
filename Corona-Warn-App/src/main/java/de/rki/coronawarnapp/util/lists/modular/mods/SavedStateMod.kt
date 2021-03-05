package de.rki.coronawarnapp.util.lists.modular.mods

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import timber.log.Timber

@Keep
class SavedStateMod<T : ModularAdapter.VH> : ModularAdapter.Module.RecyclerViewLifecycle,
    ModularAdapter.Module.Binder<T> {

    private val savedStates = mutableMapOf<String, Parcelable>()

    override fun onPostBind(adapter: ModularAdapter<T>, vh: T, pos: Int) {
        if (vh !is StateSavingVH) return
        val key = vh.savedStateKey ?: return

        savedStates.remove(key)?.let { vh.restoreState(it) }
        super.onPostBind(adapter, vh, pos)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val hostLifecycle = recyclerView.hostLifecycle ?: return

        val observer = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                savedStates.clear()

                getAllViewHolders(recyclerView).filterIsInstance<StateSavingVH>().forEach { vh ->
                    val key = vh.savedStateKey
                    val state = vh.onSaveState()
                    if (key != null && state != null) {
                        savedStates[key] = state
                    }
                }

                hostLifecycle.removeObserver(this)
            }
        }
        hostLifecycle.addObserver(observer)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        // NOOP
    }

    private val RecyclerView.hostLifecycle: Lifecycle?
        get() = try {
            findFragment<Fragment>().viewLifecycleOwner.lifecycle
        } catch (e: Exception) {
            null
        }

    private fun getAllViewHolders(recyclerView: RecyclerView): List<RecyclerView.ViewHolder> = try {
        (0..recyclerView.childCount)
            .mapNotNull { recyclerView.getChildAt(it) }
            .mapNotNull { recyclerView.getChildViewHolder(it) }
    } catch (e: Exception) {
        Timber.e(e, "getAllViewHolders() failed.")
        emptyList()
    }

    interface StateSavingVH {
        val savedStateKey: String?
        fun onSaveState(): Parcelable?
        fun restoreState(state: Parcelable)
    }
}
