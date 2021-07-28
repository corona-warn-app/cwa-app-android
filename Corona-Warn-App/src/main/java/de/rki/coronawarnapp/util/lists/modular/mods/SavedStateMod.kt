package de.rki.coronawarnapp.util.lists.modular.mods

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.ui.addLifecycleEventCallback
import timber.log.Timber

@Keep
class SavedStateMod<T : ModularAdapter.VH> :
    ModularAdapter.Module.RecyclerViewLifecycle,
    ModularAdapter.Module.Binder<T> {

    private val savedStates = mutableMapOf<String, Parcelable>()

    override fun onPostBind(adapter: ModularAdapter<T>, vh: T, pos: Int) {
        if (vh !is StateSavingVH) return
        val key = vh.savedStateKey ?: return

        vh.restoreState(savedStates.remove(key))
        super.onPostBind(adapter, vh, pos)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addLifecycleEventCallback(type = Lifecycle.Event.ON_STOP) {
            savedStates.clear()

            getAllViewHolders(recyclerView).filterIsInstance<StateSavingVH>().forEach { vh ->
                val key = vh.savedStateKey
                val state = vh.onSaveState()
                if (key != null && state != null) {
                    savedStates[key] = state
                }
            }

            true
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        // NOOP
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
        fun restoreState(state: Parcelable?)
    }
}
