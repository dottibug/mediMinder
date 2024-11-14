package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.databinding.ItemScheduledMedicationBinding
import com.example.mediminder.models.MedicationItem
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.AppUtils.formatLocalTimeTo12Hour
import com.example.mediminder.utils.AppUtils.getStatusIcon

// Medication adapter for the main activity. Displays a list of medications to be taken for a
// given date.
class MainMedicationAdapter(
    private val onUpdateStatusClick: (Long) -> Unit,
    private val onDeleteAsNeededClick: (Long) -> Unit
):
    ListAdapter<MedicationItem, MainMedicationAdapter.MedicationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemScheduledMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding, onUpdateStatusClick, onDeleteAsNeededClick)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class MedicationViewHolder(
        private val binding: ItemScheduledMedicationBinding,
        private val onUpdateStatusClick: (Long) -> Unit,
        private val onDeleteAsNeededClick: (Long) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MedicationItem) {
            val formattedTime = formatLocalTimeTo12Hour(item.time)
            binding.medicationName.text = item.medication.name
            binding.medicationDosage.text = item.dosage?.let { "${it.amount} ${it.units}" } ?: "Dosage not set"
            binding.medicationTime.text = formattedTime
            binding.medicationStatusIcon.setImageResource(getStatusIcon(item.status))
            binding.medicationStatusIcon.contentDescription = item.status.toString().lowercase()
            setStatusIconColor(item)

            // Hide update button for as-needed medications (their status is always "taken")
            if (item.medication.asNeeded) {
                binding.buttonUpdateStatus.visibility = View.GONE
                binding.buttonDeleteAsNeeded.visibility = View.VISIBLE
                binding.buttonDeleteAsNeeded.setOnClickListener { onDeleteAsNeededClick(item.logId) }
            } else {
                binding.buttonUpdateStatus.visibility = View.VISIBLE
                binding.buttonDeleteAsNeeded.visibility = View.GONE
                binding.buttonUpdateStatus.setOnClickListener { onUpdateStatusClick(item.logId) }
            }
        }

        private fun setStatusIconColor(item: MedicationItem) {
            val tintColor = getStatusIconTintColor(item.status)
            binding.medicationStatusIcon.imageTintList = binding.root.context.getColorStateList(tintColor)
        }

        private fun getStatusIconTintColor(status: MedicationStatus): Int {
            return when (status) {
                MedicationStatus.MISSED -> R.color.red
                else -> R.color.cadetGray
            }
        }
    }

    companion object {
        // DiffCallback for the medication adapter. Used to update the adapter when the data changes.
        // https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback
        private object DiffCallback : DiffUtil.ItemCallback<MedicationItem>() {
            override fun areItemsTheSame(oldItem: MedicationItem, newItem: MedicationItem): Boolean {
                return oldItem.medication.id == newItem.medication.id && oldItem.time == newItem.time
            }

           override fun areContentsTheSame(oldItem: MedicationItem, newItem: MedicationItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}