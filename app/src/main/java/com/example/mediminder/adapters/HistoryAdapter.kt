package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.databinding.ItemHistoryDayBinding
import com.example.mediminder.models.DayLogs
import com.example.mediminder.utils.Constants.DATE_PATTERN_DAY
import java.time.format.DateTimeFormatter

// Adapter for the medication log list RecyclerView in the HistoryActivity.
class HistoryAdapter : ListAdapter<DayLogs, HistoryAdapter.DayViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemHistoryDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(private val binding: ItemHistoryDayBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN_DAY) // Mon, Jan 1
        private val logsAdapter = LogsAdapter()

        init {
            binding.logsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = logsAdapter
            }
        }

        fun bind(dayLogs: DayLogs) {
            binding.dateHeader.text = dayLogs.date.format(dateFormatter)

            if (dayLogs.logs.isEmpty()) { showLogs(false) }
            else {
                showLogs(true)
                logsAdapter.submitList(dayLogs.logs)
            }
        }

        private fun showLogs(show: Boolean) {
            binding.noMedicationsText.visibility = if (show) View.GONE else View.VISIBLE
            binding.logsList.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DayLogs>() {
            override fun areItemsTheSame(oldItem: DayLogs, newItem: DayLogs): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: DayLogs, newItem: DayLogs): Boolean {
                return oldItem == newItem
            }
        }
    }
}