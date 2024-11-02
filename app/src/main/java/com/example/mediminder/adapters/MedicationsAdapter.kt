package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.data.local.classes.MedicationIcon
import com.example.mediminder.data.repositories.MedicationWithDetails
import com.example.mediminder.databinding.ItemMedicationBinding

class MedicationsAdapter(
    private val onViewClick: (Long) -> Unit,
    private val onEditClick: (Long) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<MedicationWithDetails, MedicationsAdapter.MedicationViewHolder>(DiffCallback) {

    class MedicationViewHolder(
        private val binding: ItemMedicationBinding,
        private val onViewClick: (Long) -> Unit,
        private val onEditClick: (Long) -> Unit,
        private val onDeleteClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(medicationDetails: MedicationWithDetails) {
            binding.medicationName.text = medicationDetails.medication.name


            val iconResId = when (medicationDetails.medication.icon) {
                MedicationIcon.CAPSULE -> R.drawable.capsule
                MedicationIcon.DROP -> R.drawable.drop
                MedicationIcon.INHALER -> R.drawable.inhaler
                MedicationIcon.INJECTION -> R.drawable.injection
                MedicationIcon.LIQUID -> R.drawable.liquid
                MedicationIcon.TABLET -> R.drawable.tablet
            }
            binding.medicationIcon.setImageResource(iconResId)

            binding.buttonViewMedication.setOnClickListener {
                onViewClick(medicationDetails.medication.id)
            }
            binding.buttonEditMedication.setOnClickListener {
                onEditClick(medicationDetails.medication.id)
            }
            binding.buttonDeleteMedication.setOnClickListener {
                onDeleteClick(medicationDetails.medication.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicationViewHolder(binding, onViewClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MedicationWithDetails>() {
            override fun areItemsTheSame(oldItem: MedicationWithDetails, newItem: MedicationWithDetails): Boolean {
                return oldItem.medication.id == newItem.medication.id
            }

            override fun areContentsTheSame(oldItem: MedicationWithDetails, newItem: MedicationWithDetails): Boolean {
                return oldItem == newItem
            }
        }
    }
}