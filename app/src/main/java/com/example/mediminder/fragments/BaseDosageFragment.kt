package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediminder.databinding.FragmentBaseDosageBinding
import com.example.mediminder.models.DosageData
import com.example.mediminder.utils.Constants.DOSAGE_DEFAULT_UNIT

// BaseDosageFragment is the base class for all dosage fragments
abstract class BaseDosageFragment : Fragment() {
    protected lateinit var binding: FragmentBaseDosageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBaseDosageBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Get dosage data from the UI
    fun getDosageData(): DosageData {
        return DosageData(
            dosageAmount = binding.inputDosage.text.toString(),
            dosageUnits = getDosageUnits()
        )
    }

    private fun getDosageUnits(): String {
        val units = binding.dosageUnitsDropdown.text.toString()
        return if (units.isEmpty()) { DOSAGE_DEFAULT_UNIT } else { units }
    }
}