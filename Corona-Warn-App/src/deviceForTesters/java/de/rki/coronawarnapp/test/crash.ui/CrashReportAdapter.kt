package de.rki.coronawarnapp.test.crash.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.bugreporting.event.BugEvent

import de.rki.coronawarnapp.databinding.ViewCrashreportListItemBinding

class CrashReportAdapter(private val itemClickListener: (bugEvent: BugEvent) -> Unit) : RecyclerView.Adapter<CrashReportAdapter.CrashHolder>() {

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
        holder.itemView.setOnClickListener { itemClickListener(crashReport) }
    }

    override fun getItemCount() = crashReports.size

    fun updateCrashReports(crashReportList: List<BugEvent>) {
        crashReports = crashReportList
        notifyDataSetChanged()
    }

    class CrashHolder(private val binding: ViewCrashreportListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(crashReport: BugEvent, pos: Int) {
            binding.crashReport = crashReport
            binding.pos = (pos + 1)
        }
    }
}
