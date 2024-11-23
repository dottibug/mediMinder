package com.example.mediminder.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.mediminder.R
import com.example.mediminder.databinding.FragmentBaseMedicationInfoBinding
import com.example.mediminder.models.MedicationData
import com.example.mediminder.models.MedicationIcon
import com.example.mediminder.models.MedicationStatus
import com.example.mediminder.viewmodels.AppViewModel
import kotlinx.coroutines.launch

abstract class BaseMedicationInfoFragment: Fragment() {
    protected lateinit var binding: FragmentBaseMedicationInfoBinding
    protected val appViewModel: AppViewModel by activityViewModels { AppViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentBaseMedicationInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupObservers()
    }

    private fun setupListener() {
        binding.asScheduledSwitch.setOnCheckedChangeListener { _, isChecked ->
            appViewModel.setAsScheduled(isChecked)
        }
    }

    // Collect state flow from medication view model when the fragment is in the STARTED state
    private fun setupObservers() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { appViewModel.medication.asScheduled.collect { asScheduled ->
                        setSwitchState(asScheduled)
                        appViewModel.setAsScheduled(asScheduled)
                        updateSwitchUI(asScheduled)
                        toggleSwitchMessage(asScheduled)
                    }
                }
            }
        }
    }

    // Prevent infinite loop while setting the switch state
    private fun setSwitchState(asScheduled: Boolean) {
        if (binding.asScheduledSwitch.isChecked != asScheduled) {
            binding.asScheduledSwitch.isChecked = asScheduled
        }
    }

    // Update appearance of the switch based on the switch state
    private fun updateSwitchUI(asScheduled: Boolean) {
        binding.asScheduledSwitch.apply {
            text = getSwitchString(asScheduled)
            thumbTintList = getSwitchTint(asScheduled)
        }
    }

    // Helper function to get the switch text based on the switch state
    private fun getSwitchString(asScheduled: Boolean): String {
        return if (asScheduled) {
            resources.getString(R.string.switch_take_as_scheduled)
        } else {
            resources.getString(R.string.switch_take_as_needed)
        }
    }

    // Helper function to get the switch tint based on the switch state
    private fun getSwitchTint(asScheduled: Boolean): ColorStateList {
        return if (asScheduled) {
            resources.getColorStateList(R.color.indigoDye, null)
        } else {
            resources.getColorStateList(R.color.cadetGray, null)
        }
    }

    // Show or hide the message based on the switch state
    private fun toggleSwitchMessage(asScheduled: Boolean) {
        binding.asNeededMessage.visibility = if (asScheduled) View.GONE else View.VISIBLE
    }

    // Get medication data from the UI
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

    // Helper function to get the medication icon from the dropdown
    private fun getIcon(iconName: String): MedicationIcon {
        return if (iconName.isEmpty()) { MedicationIcon.TABLET }
        else { MedicationIcon.valueOf(iconName.uppercase()) }
    }
}