package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ItemMedicationBinding
import java.time.LocalTime

// Medication adapter for the main activity. Displays a list of medications to be taken for a
// given date.
class MainMedicationAdapter(
    private val onUpdateStatusClick: (Long) -> Unit
) :
    ListAdapter<Triple<Medication, Dosage?, LocalTime>, MainMedicationAdapter.MedicationViewHolder>(DiffCallback) {
//    RecyclerView.Adapter<MainMedicationAdapter.MedicationViewHolder>() {

//    private var medications: List<Pair<Medication, Dosage?>> = emptyList()

//    fun setMedications(newMedications: List<Pair<Medication, Dosage?>>) {
//        medications = newMedications
//        notifyDataSetChanged()
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding, onUpdateStatusClick)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val (medication, dosage, time) = getItem(position)
        holder.bind(medication, dosage, time)
    }

//    override fun getItemCount(): Int = medications.size

    class MedicationViewHolder(
        private val binding: ItemMedicationBinding,
        private val onUpdateStatusClick: (Long) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication, dosage: Dosage?, time: LocalTime) {
            binding.medicationName.text = medication.name
            binding.medicationDosage.text = dosage?.let { "${it.amount} ${it.units}" } ?: "Dosage not set"
            binding.medicationTime.text = time.toString()
            binding.buttonUpdateStatus.setOnClickListener { onUpdateStatusClick(medication.id) }
            // todo set image resource for the status icon based on the medication status
        }
    }

    companion object {
        // DiffCallback for the medication adapter. Used to update the adapter when the data changes.
        // https://developer.android.com/reference/androidx/recyclerview/widget/DiffUtil.ItemCallback
        private object DiffCallback : DiffUtil.ItemCallback<Triple<Medication, Dosage?, LocalTime>>() {
            override fun areItemsTheSame(
                oldItem: Triple<Medication, Dosage?, LocalTime>,
                newItem: Triple<Medication, Dosage?, LocalTime>
            ): Boolean {
                return oldItem.first.id == newItem.first.id && oldItem.third == newItem.third
            }

            override fun areContentsTheSame(
                oldItem: Triple<Medication, Dosage?, LocalTime>,
                newItem: Triple<Medication, Dosage?, LocalTime>
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}