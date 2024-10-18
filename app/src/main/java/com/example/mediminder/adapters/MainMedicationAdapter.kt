package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.data.repositories.MedicationWithDosage
import com.example.mediminder.databinding.ItemMedicationBinding

class MainMedicationAdapter: RecyclerView.Adapter<MainMedicationAdapter.MedicationViewHolder>() {
    private var medications: List<MedicationWithDosage> = emptyList()

    fun updateMedications(newMedications: List<MedicationWithDosage>) {
        medications = newMedications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainMedicationAdapter.MedicationViewHolder, position: Int) {
        val medicationWithDosage = medications[position]
        holder.bind(medicationWithDosage)
    }

    override fun getItemCount(): Int = medications.size

    class MedicationViewHolder(private val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medicationWithDosage: MedicationWithDosage) {
            val medication = medicationWithDosage.medication
            val dosage = medicationWithDosage.dosage

            binding.medicationName.text = medication.name
            binding.medicationNotes.text = medication.notes
            binding.medicationDosage.text = "${dosage?.amount} ${dosage?.units}" ?: "No dosage info"

        }
    }

}