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
import com.example.mediminder.utils.Constants.DOSAGE_DEFAULT_UNIT

/**
 * Medication adapter for MainActivity. Displays a list of medications to be taken for a given date.
 * Users can update the status of scheduled medications to taken, skipped, or missed. Users can also
 * delete as-needed medications.
 * @param onUpdateStatusClick Callback for clicking the update status button
 * @param onDeleteAsNeededClick Callback for clicking the delete as-needed button
 */
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

    /**
     * ViewHolder for medication items
     * @param binding Binding for the medication item layout
     * @param onUpdateStatusClick Callback for clicking the update status button
     * @param onDeleteAsNeededClick Callback for clicking the delete as-needed button
     */
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

        /**
         * Gets the dosage string for the medication
         * @param dosage Dosage of the medication
         * @return Dosage string
         */
        private fun getDosageString(dosage: Dosage?): String {
            if (dosage == null) return DOSAGE_NOT_SET
            if (dosage.amount === null) return DOSAGE_NOT_SET
            val amount = dosage.amount
            val units = dosage.units ?: DOSAGE_DEFAULT_UNIT
            return "$amount $units"
        }

        /**
         * Gets the color of the status icon based on the medication status
         * @param status Medication status
         */
        private fun getStatusIconTintColor(status: MedicationStatus): Int {
            return when (status) {
                MedicationStatus.MISSED -> R.color.red
                else -> R.color.cadetGray
            }
        }

        /**
         * Sets up the buttons based on the medication type
         * As-needed: Show delete button
         * Scheduled: Show update button
         */
        private fun setupButtons(item: MedicationItem) {
            if (item.medication.asNeeded) { setupDeleteButton(item) }
            else { setupUpdateButton(item) }
        }

        /**
         * Sets up the delete button for as-needed medications
         */
        private fun setupDeleteButton(item: MedicationItem) {
            updateButton.visibility = View.GONE
            deleteButton.apply {
                visibility = View.VISIBLE
                setOnClickListener { onDeleteAsNeededClick(item.logId) }
            }
        }

        /**
         * Sets up the update button for scheduled medications
         */
        private fun setupUpdateButton(item: MedicationItem) {
            deleteButton.visibility = View.GONE
            updateButton.apply {
                visibility = View.VISIBLE
                isEnabled = item.canUpdateStatus
                alpha = if (item.canUpdateStatus) 1.0f else 0.5f  // Reduce opacity if not enabled
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