package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentBaseMedicationInfoBinding
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import kotlinx.coroutines.launch

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.asNeededSwitch.setOnCheckedChangeListener { _, isChecked ->
            medicationViewModel.setAsNeeded(isChecked)
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            medicationViewModel.asNeeded.collect { asNeeded ->
                // Prevent infinite loop of setting the switch state
                if (binding.asNeededSwitch.isChecked != asNeeded) {
                    binding.asNeededSwitch.isChecked = asNeeded
                }

                medicationViewModel.setAsNeeded(asNeeded)

                // Update appearance of the switch based on the switch state
                binding.asNeededSwitch.thumbTintList = if (asNeeded) {
                    resources.getColorStateList(R.color.indigoDye, null)
                } else {
                    resources.getColorStateList(R.color.cadetGray, null)
                }

                // Toggle the visibility of the message based on the switch state
                binding.asNeededMessage.visibility = if (asNeeded) View.VISIBLE else View.GONE
            }
        }
    }

    private fun getIcon(iconName: String): MedicationIcon {
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