package de.rki.coronawarnapp.test.crash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.bugreporting.event.BugEvent


import de.rki.coronawarnapp.databinding.ViewCrashreportListItemBinding

class CrashReportAdapter(private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<CrashReportAdapter.CrashHolder>() {

    private var crashReports = listOf<BugEvent>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrashHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewCrashreportListItemBinding.inflate(inflater)
        return CrashHolder(
            binding
        )
    }

    override fun onBindViewHolder(holder: CrashHolder, position: Int) {
        val crashReport = crashReports[position]
        holder.bind(crashReport, position)
        holder.itemView.setOnClickListener { itemClickListener.crashReportClicked(crashReport) }
    }

    override fun getItemCount() = crashReports.size

    fun updateCrashReports(crashReportList: List<BugEvent>) {
        crashReports = crashReportList
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun crashReportClicked(crashReport: BugEvent)
    }

    class CrashHolder(private val binding: ViewCrashreportListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(crashReport: BugEvent, pos: Int) {
            binding.crashReport = crashReport
            binding.pos = (pos + 1)
        }
    }
}
