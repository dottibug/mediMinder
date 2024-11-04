package com.example.mediminder.fragments

import androidx.fragment.app.activityViewModels
import com.example.mediminder.viewmodels.BaseMedicationViewModel

class AddMedicationInfoFragment : BaseMedicationInfoFragment() {
    override val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }
}