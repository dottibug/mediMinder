package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediminder.databinding.FragmentAddMedicationDosageBinding
import com.example.mediminder.viewmodels.DosageData

class AddMedicationDosageFragment : Fragment() {
    private lateinit var binding: FragmentAddMedicationDosageBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMedicationDosageBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun getDosageData(): DosageData {
        return DosageData(
            dosageAmount = binding.inputDosage.text.toString(),
            dosageUnits = binding.dosageUnitsDropdown.text.toString()
        )
    }
}