package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.data.local.classes.Dosage
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ItemMedicationBinding

class MainMedicationAdapter(
    private val onUpdateStatusClick: (Long) -> Unit
) : RecyclerView.Adapter<MainMedicationAdapter.MedicationViewHolder>() {

    private var medications: List<Pair<Medication, Dosage?>> = emptyList()

    fun setMedications(newMedications: List<Pair<Medication, Dosage?>>) {
        medications = newMedications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding, onUpdateStatusClick)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val (medication, dosage) = medications[position]
        holder.bind(medication, dosage)
    }

    override fun getItemCount(): Int = medications.size

    class MedicationViewHolder(
        private val binding: ItemMedicationBinding,
        private val onUpdateStatusClick: (Long) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication, dosage: Dosage?) {
            binding.medicationName.text = medication.name
            binding.medicationNotes.text = medication.notes
            binding.medicationDosage.text = dosage?.let { "${it.amount} ${it.units}" } ?: "Dosage not set"

            binding.buttonUpdateStatus.setOnClickListener {
                // todo: use the medication status from database; MedicationStatus.TAKEN is plaecholeder for development
                onUpdateStatusClick(medication.id)
            }


            // todo set image resource for the status icon based on the medication status

        }
    }
}