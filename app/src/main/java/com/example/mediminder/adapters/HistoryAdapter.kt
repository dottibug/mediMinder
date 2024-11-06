package com.example.mediminder.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediminder.data.local.classes.Medication
import com.example.mediminder.databinding.ItemHistoryDayBinding

class HistoryAdapter : ListAdapter<Medication, HistoryAdapter.DayViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemHistoryDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
      holder.bind(getItem(position))
    }

    class DayViewHolder(private val binding: ItemHistoryDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: Medication) {
            Log.d("HistoryAdapter testcat", "Binding day: $day")
        }
    }

    override fun submitList(list: List<Medication>?) {
        //
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Medication>() {
            override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
                return oldItem == newItem
            }
        }
    }
}