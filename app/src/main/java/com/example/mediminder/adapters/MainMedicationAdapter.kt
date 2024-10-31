package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.data.repositories.MedicationItem
import com.example.mediminder.databinding.ItemMedicationBinding
import com.example.mediminder.utils.StatusIconUtil
import java.time.LocalTime

// Medication adapter for the main activity. Displays a list of medications to be taken for a
// given date.
class MainMedicationAdapter(
    private val onUpdateStatusClick: (Long) -> Unit
) :
    ListAdapter<MedicationItem, MainMedicationAdapter.MedicationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding, onUpdateStatusClick)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val (medication, dosage, time, status) = getItem(position)
        holder.bind(medication, dosage, time, status)
    }

//    override fun getItemCount(): Int = medications.size

    class MedicationViewHolder(
        private val binding: ItemMedicationBinding,
        private val onUpdateStatusClick: (Long) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication, dosage: Dosage?, time: LocalTime, status: MedicationStatus) {
            binding.medicationName.text = medication.name
            binding.medicationDosage.text = dosage?.let { "${it.amount} ${it.units}" } ?: "Dosage not set"
            binding.medicationTime.text = time.toString()
            binding.medicationStatusIcon.setImageResource(StatusIconUtil.getStatusIcon(status))
            binding.buttonUpdateStatus.setOnClickListener { onUpdateStatusClick(medication.id) }
        }
    }

    companion object {
        // DiffCallback for the medication adapter. Used to update the adapter when the data changes.
        // https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback
        private object DiffCallback : DiffUtil.ItemCallback<MedicationItem>() {
            override fun areItemsTheSame(
                oldItem: MedicationItem,
                newItem: MedicationItem
            ): Boolean {
                return oldItem.medication.id == newItem.medication.id && oldItem.time == newItem.time
            }

           override fun areContentsTheSame(
                oldItem: MedicationItem, newItem: MedicationItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}