package com.example.mediminder.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.databinding.ItemDateSelectorBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainDateSelectorAdapter(
    private var dates: List<LocalDate>,
    private val onDateSelected: (LocalDate) -> Unit
): RecyclerView.Adapter<MainDateSelectorAdapter.DateViewHolder>() {

    private var selectedPosition = findTodayIndex()
    private val dateFormatter = DateTimeFormatter.ofPattern("d EEE") // ex. 11 Tue

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
        holder.binding.dateText.text = date.format(dateFormatter)
        holder.binding.root.isSelected = position == selectedPosition

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