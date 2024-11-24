package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.databinding.ItemHistoryLogBinding
import com.example.mediminder.models.MedicationLogWithDetails
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.AppUtils.getStatusIcon
import com.example.mediminder.utils.Constants.TIME_PATTERN
import java.time.format.DateTimeFormatter

/**
 * Adapter for the medication log list RecyclerView in the HistoryActivity.
 */
class LogsAdapter: ListAdapter<MedicationLogWithDetails, LogsAdapter.LogViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemHistoryLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for the medication log list items
     */
    class LogViewHolder(private val binding: ItemHistoryLogBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)

        fun bind(medLog: MedicationLogWithDetails) {
            binding.statusIcon.setImageResource(getStatusIcon(medLog.log.status))
            setStatusIconColor(medLog.log.status)
            binding.medicationName.text = medLog.name

            if (medLog.dosageUnits == null || medLog.dosageAmount == null) {
                val dosageAmount = medLog.log.asNeededDosageAmount
                val dosageUnits = medLog.log.asNeededDosageUnit
                binding.dosageAndTime.text = itemView.context.getString(
                    R.string.history_log_dosage_time,
                    dosageAmount,
                    dosageUnits,
                    medLog.log.plannedDatetime.format(timeFormatter)
                )
            } else {
                binding.dosageAndTime.text = itemView.context.getString(
                    R.string.history_log_dosage_time,
                    medLog.dosageAmount,
                    medLog.dosageUnits,
                    medLog.log.plannedDatetime.format(timeFormatter)
                )
            }
        }

        /**
         * Sets the color of the status icon based on the medication status
         */
        private fun setStatusIconColor(status: MedicationStatus) {
            val tintColor = when (status) {
                MedicationStatus.MISSED -> R.color.red
                else -> R.color.jet
            }
            binding.statusIcon.imageTintList = binding.root.context.getColorStateList(tintColor)
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