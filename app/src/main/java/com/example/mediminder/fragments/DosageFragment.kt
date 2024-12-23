package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mediminder.databinding.FragmentDosageBinding
import com.example.mediminder.models.DosageData
import com.example.mediminder.utils.Constants.DOSAGE_DEFAULT_UNIT
import com.example.mediminder.viewmodels.AppViewModel

/**
 * Base class for dosage fragments.
 */
open class DosageFragment : Fragment() {
    protected lateinit var binding: FragmentDosageBinding
    protected val appViewModel: AppViewModel by activityViewModels { AppViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentDosageBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Get dosage data from the UI.
     */
    fun getDosageData(): DosageData {
        return DosageData(
            dosageAmount = binding.inputDosage.text.toString(),
            dosageUnits = getDosageUnits()
        )
    }

    /**
     * Get dosage units as string (set default to "units" if not specified).
     */
    private fun getDosageUnits(): String {
        val units = binding.dosageUnitsDropdown.text.toString()
        return if (units.isEmpty()) { DOSAGE_DEFAULT_UNIT } else { units }
    }
}