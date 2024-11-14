package com.example.mediminder.fragments

import androidx.fragment.app.activityViewModels
import com.example.mediminder.viewmodels.BaseMedicationViewModel
import com.example.mediminder.viewmodels.BaseReminderViewModel
import com.example.mediminder.viewmodels.BaseScheduleViewModel

class AddMedicationReminderFragment : BaseReminderFragment() {
    override val reminderViewModel: BaseReminderViewModel by activityViewModels()
    override val medicationViewModel: BaseMedicationViewModel by activityViewModels { BaseMedicationViewModel.Factory }
    override val scheduleViewModel: BaseScheduleViewModel by activityViewModels()
}