package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.databinding.ItemHistoryLogBinding
import com.example.mediminder.models.MedicationLogWithDetails
import com.example.mediminder.utils.AppUtils.getStatusIcon
import java.time.format.DateTimeFormatter

class LogsAdapter: ListAdapter<MedicationLogWithDetails, LogsAdapter.LogViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemHistoryLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LogViewHolder(private val binding: ItemHistoryLogBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

        fun bind(medLog: MedicationLogWithDetails) {
            binding.statusIcon.setImageResource(getStatusIcon(medLog.log.status))
            binding.medicationName.text = medLog.name
            binding.dosageAndTime.text = itemView.context.getString(
                R.string.history_log_dosage_time,
                medLog.dosageAmount,
                medLog.dosageUnits,
                medLog.log.plannedDatetime.format(timeFormatter)
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