package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.R
import com.example.mediminder.databinding.ItemDateSelectorBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainDateSelectorAdapter(
    private var dates: List<LocalDate>,
    private val onDateSelected: (LocalDate) -> Unit
): RecyclerView.Adapter<MainDateSelectorAdapter.DateViewHolder>() {
    private var selectedPosition = findTodayIndex()

    fun updateDates(newDates: List<LocalDate>) {
        dates = newDates
        selectedPosition = findTodayIndex()
        notifyDataSetChanged()
    }

    // Reference to the custom view for each date item in the dates list
    inner class DateViewHolder(val binding: ItemDateSelectorBinding): RecyclerView.ViewHolder(binding.root)

    // Create a new ViewHolder for each date item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    // Bind the data for each date item to the ViewHolder
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dates[position]
        val dateNum = date.format(DateTimeFormatter.ofPattern("d"))
        val dateText = date.format(DateTimeFormatter.ofPattern("EEE"))

        holder.binding.dateNum.text = dateNum
        holder.binding.dateText.text = dateText

        // Check if the current position is the selected date
        if (position == selectedPosition) {
            holder.binding.root.strokeWidth = 12
            holder.binding.root.setCardBackgroundColor(holder.binding.root.context.getColor(R.color.white))
            holder.binding.root.strokeColor = holder.binding.root.context.getColor(R.color.indigoDye)
        } else {
            holder.binding.root.strokeWidth = 0
            holder.binding.root.strokeColor = holder.binding.root.context.getColor(android.R.color.transparent)
            holder.binding.root.setCardBackgroundColor(holder.binding.root.context.getColor(R.color.cadetGrayXLt))

        }

        // Set a click listener to handle date selection
        holder.binding.root.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onDateSelected(date)
        }
    }

    // Return size of dates list (required by RecyclerView)
    override fun getItemCount(): Int { return dates.size }

    // Find the index of today's date in the list to set it as the default selected date
    private fun findTodayIndex(): Int {
        val today = LocalDate.now()
        return dates.indexOfFirst { it == today }.takeIf { it != -1 } ?: RecyclerView.NO_POSITION
    }

}