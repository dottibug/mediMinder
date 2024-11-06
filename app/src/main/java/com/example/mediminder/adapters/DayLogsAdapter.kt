package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.data.repositories.MedicationLogWithDetails
import com.example.mediminder.databinding.ItemHistoryLogBinding
import com.example.mediminder.utils.StatusIconUtil
import java.time.format.DateTimeFormatter

class DayLogsAdapter: ListAdapter<MedicationLogWithDetails, DayLogsAdapter.LogViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemHistoryLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LogViewHolder(private val binding: ItemHistoryLogBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

        fun bind(log: MedicationLogWithDetails) {
            binding.statusIcon.setImageResource(StatusIconUtil.getStatusIcon(log.status))
            binding.medicationName.text = log.medication.name
            binding.dosageAndTime.text = itemView.context.getString(
                R.string.history_log_dosage_time,
                log.dosage.amount,
                log.dosage.units,
                log.plannedDateTime.format(timeFormatter)
            )
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MedicationLogWithDetails>() {
            override fun areItemsTheSame(oldItem: MedicationLogWithDetails, newItem: MedicationLogWithDetails): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MedicationLogWithDetails, newItem: MedicationLogWithDetails): Boolean {
                return oldItem == newItem
            }
        }
    }
}