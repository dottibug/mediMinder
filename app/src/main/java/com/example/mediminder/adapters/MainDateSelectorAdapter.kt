package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.databinding.ItemDateSelectorBinding
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Adapter for the date selector RecyclerView in the MainActivity.
class MainDateSelectorAdapter(
    private val onDateSelected: (LocalDate) -> Unit
): ListAdapter<LocalDate, MainDateSelectorAdapter.DateViewHolder>(DiffCallback) {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = getItem(position)
        holder.bind(date, position == selectedPosition) {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onDateSelected(date)
        }
    }

    fun updateSelectedPosition() {
        selectedPosition = currentList.indexOfFirst { it == LocalDate.now() }
            .takeIf { it != -1 } ?: RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    class DateViewHolder(val binding: ItemDateSelectorBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(
            date: LocalDate,
            isSelected: Boolean,
            onDateSelected: (LocalDate) -> Unit
        ) {
            with(binding) {
                dateNum.text = date.format(DATE_NUMBER_FORMATTER)
                dateText.text = date.format(DATE_TEXT_FORMATTER)
                updateDateItem(root, isSelected)
                root.setOnClickListener { onDateSelected(date) }
            }
        }

        private fun updateDateItem(card: MaterialCardView, isSelected: Boolean) {
            with(card) {
                val indigoDye = context.getColor(R.color.indigoDye)
                val transparent = context.getColor(android.R.color.transparent)
                val cadetGrayXLt = context.getColor(R.color.cadetGrayXLt)
                val white = context.getColor(R.color.white)

                strokeWidth = if (isSelected) 12 else 0
                strokeColor = if (isSelected) indigoDye else transparent
                setCardBackgroundColor(if (isSelected) white else cadetGrayXLt)
            }
        }
    }

    companion object {
        private val DATE_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("d")
        private val DATE_TEXT_FORMATTER = DateTimeFormatter.ofPattern("EEE")

        private val DiffCallback = object : DiffUtil.ItemCallback<LocalDate>() {
            override fun areItemsTheSame(oldItem: LocalDate, newItem: LocalDate): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: LocalDate, newItem: LocalDate): Boolean {
                return oldItem == newItem
            }
        }
    }
}