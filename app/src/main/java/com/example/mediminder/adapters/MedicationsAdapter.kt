package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.databinding.ItemMedicationBinding
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.MedicationWithDetails

/**
 * Adapter for the medication list RecyclerView in the MedicationsActivity
 * @param onViewClick Callback for clicking the view button
 * @param onEditClick Callback for clicking the edit button
 * @param onDeleteClick Callback for clicking the delete button
 */
class MedicationsAdapter(
    private val onViewClick: (Long) -> Unit,
    private val onEditClick: (Long) -> Unit,
    private val onDeleteClick: (Long) -> Unit
) : ListAdapter<MedicationWithDetails, MedicationsAdapter.MedicationViewHolder>(DiffCallback) {

    /**
     * ViewHolder for the medication list items
     */
    class MedicationViewHolder(
        private val binding: ItemMedicationBinding,
        private val onViewClick: (Long) -> Unit,
        private val onEditClick: (Long) -> Unit,
        private val onDeleteClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Binds medication data to the view holder
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

    /**
     * Creates a new view holder for the medication list items
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val binding = ItemMedicationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MedicationViewHolder(binding, onViewClick, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        /**
         * DiffCallback for the medication list updates
         */
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