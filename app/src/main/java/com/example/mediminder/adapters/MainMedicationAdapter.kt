package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.databinding.ItemScheduledMedicationBinding
import com.example.mediminder.models.MedicationItem
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.utils.AppUtils.formatLocalTimeTo12Hour
import com.example.mediminder.utils.AppUtils.getStatusIcon

// Medication adapter for MainActivity. Displays a list of medications to be taken for a given date.
class MainMedicationAdapter(
    private val onUpdateStatusClick: (Long) -> Unit,
    private val onDeleteAsNeededClick: (Long) -> Unit
): ListAdapter<MedicationItem, MainMedicationAdapter.MedicationViewHolder>(DiffCallback) {

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
        private val statusIcon = binding.medicationStatusIcon
        private val updateButton = binding.buttonUpdateStatus
        private val deleteButton = binding.buttonDeleteAsNeeded

        fun bind(item: MedicationItem) {
            with (binding) {
                medicationName.text = item.medication.name
                medicationDosage.text = getDosageString(item.dosage)
                medicationTime.text = formatLocalTimeTo12Hour(item.time)
            }

            with (statusIcon) {
                setImageResource(getStatusIcon(item.status))
                contentDescription = item.status.toString().lowercase()
                imageTintList = context.getColorStateList(getStatusIconTintColor(item.status))
            }

            setupButtons(item)
        }

        private fun getDosageString(dosage: Dosage?): String {
            return dosage?.let { "${it.amount} ${it.units}" } ?: DOSAGE_NOT_SET
        }

        private fun getStatusIconTintColor(status: MedicationStatus): Int {
            return when (status) {
                MedicationStatus.MISSED -> R.color.red
                else -> R.color.cadetGray
            }
        }

        private fun setupButtons(item: MedicationItem) {
            if (item.medication.asNeeded) { setupDeleteButton(item) }
            else { setupUpdateButton(item) }
        }

        private fun setupDeleteButton(item: MedicationItem) {
            updateButton.visibility = View.GONE
            deleteButton.apply {
                visibility = View.VISIBLE
                setOnClickListener { onDeleteAsNeededClick(item.logId) }
            }
        }

        private fun setupUpdateButton(item: MedicationItem) {
            deleteButton.visibility = View.GONE
            updateButton.apply {
                visibility = View.VISIBLE
                setOnClickListener { onUpdateStatusClick(item.logId) }
            }
        }
    }

    companion object {
        private const val DOSAGE_NOT_SET = "Dosage not set"

        // DiffCallback for the medication adapter. Used to update the adapter when the data changes.
        // https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback
        private object DiffCallback: DiffUtil.ItemCallback<MedicationItem>() {
            override fun areItemsTheSame(oldItem: MedicationItem, newItem: MedicationItem): Boolean {
                return oldItem.logId == newItem.logId
            }

           override fun areContentsTheSame(oldItem: MedicationItem, newItem: MedicationItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}