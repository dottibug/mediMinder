package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediminder.data.local.classes.MedicationIcon
import com.example.mediminder.data.local.classes.MedicationStatus
import com.example.mediminder.databinding.FragmentBaseMedicationInfoBinding
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.MedicationData

abstract class BaseMedicationInfoFragment: Fragment() {
    protected lateinit var binding: FragmentBaseMedicationInfoBinding
    protected abstract val medicationViewModel: BaseMedicationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBaseMedicationInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun getIcon(iconName: String): MedicationIcon {
        return MedicationIcon.valueOf(iconName.uppercase())
    }

    fun getMedicationData(): MedicationData {
        val iconName = binding.medicationIconDropdown.text.toString().uppercase()
        val icon = getIcon(iconName)

        return MedicationData(
            name = binding.inputMedName.text.toString(),
            doctor = binding.inputDoctor.text.toString(),
            notes = binding.inputMedNotes.text.toString(),
            icon = icon,
            status = MedicationStatus.PENDING,
        )
    }
}