package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.crash.CrashModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_crashreporter_overview.*

class SettingsCrashReporterFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            SettingsCrashReporterFragment().apply {
                arguments = Bundle().apply {
                    // putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    private lateinit var adapter: MyCrashesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            // columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_crashreporter_overview, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val items = listOf(
            CrashModel("Crash #0", "Nullpointer wie in alten Zeiten"),
            CrashModel("Crash #1", "Source"),
            CrashModel("Crash #2", "Source"),
            CrashModel("Crash #3", "Source"),
            CrashModel("Crash #4", "Source"),
            CrashModel("Crash #5", "Source")
        )

        adapter = MyCrashesAdapter()
        adapter.replaceItems(items)
        list.adapter = adapter
    }

    // ****************ADAPTER***************

    class MyCrashesAdapter : RecyclerView.Adapter<MyCrashesAdapter.ViewHolder>() {
        private var items = listOf<CrashModel>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_crashreport_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            // holder.contentTextView.text = item.content
            // holder.sourceTextView.text = item.source
        }

        fun replaceItems(items: List<CrashModel>) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
            LayoutContainer
    }
}
