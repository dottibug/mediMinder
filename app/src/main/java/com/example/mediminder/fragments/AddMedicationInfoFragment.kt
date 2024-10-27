package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediminder.databinding.FragmentAddMedicationInfoBinding
import com.example.mediminder.viewmodels.MedicationData

class AddMedicationInfoFragment : Fragment() {
    private lateinit var binding: FragmentAddMedicationInfoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMedicationInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun getMedicationData(): MedicationData {
        return MedicationData(
            name = binding.inputMedName.text.toString(),
            doctor = binding.inputDoctor.text.toString(),
            notes = binding.inputMedNotes.text.toString()
        )
    }
}

