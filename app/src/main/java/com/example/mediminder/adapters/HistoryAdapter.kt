package com.example.mediminder.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.databinding.ItemHistoryDayBinding
import com.example.mediminder.models.DayLogs
import java.time.format.DateTimeFormatter

class HistoryAdapter : ListAdapter<DayLogs, HistoryAdapter.DayViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        Log.d("HistoryAdapter testcat", "Creating new ViewHolder")

        val binding = ItemHistoryDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("HistoryAdapter testcat", "Binding item for date: ${item.date}")

        holder.bind(item)
    }

    class DayViewHolder(private val binding: ItemHistoryDayBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d") // Mon, Jan 1
        private val logsAdapter = LogsAdapter()

        init {
            binding.logsList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = logsAdapter
            }
        }

        fun bind(dayLogs: DayLogs) {
            binding.dateHeader.text = dayLogs.date.format(dateFormatter)

            if (dayLogs.logs.isEmpty()) {
                binding.noMedicationsText.visibility = View.VISIBLE
                binding.logsList.visibility = View.GONE
            } else {
                binding.noMedicationsText.visibility = View.GONE
                binding.logsList.visibility = View.VISIBLE
                logsAdapter.submitList(dayLogs.logs)
            }
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