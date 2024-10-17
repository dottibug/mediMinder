package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ItemMedicationBinding

class MainMedicationAdapter: RecyclerView.Adapter<MainMedicationAdapter.MedicationViewHolder>() {
    private var medications: List<Medication> = emptyList()

    fun updateMedications(newMedications: List<Medication>) {
        medications = newMedications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainMedicationAdapter.MedicationViewHolder, position: Int) {
        val medication = medications[position]
        holder.bind(medication)
    }

    override fun getItemCount(): Int = medications.size

    class MedicationViewHolder(private val binding: ItemMedicationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(medication: Medication) {
            binding.medicationName.text = medication.name
            binding.medicationNotes.text = medication.notes
        }
    }

}