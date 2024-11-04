package com.example.mediminder.fragments

import androidx.fragment.app.activityViewModels
import com.example.mediminder.viewmodels.BaseMedicationViewModel

class AddMedicationScheduleFragment : BaseScheduleFragment() {
    override val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }
}