package com.example.mediminder.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.mediminder.databinding.FragmentAddMedicationDialogBinding
import com.example.mediminder.utils.WindowInsetsUtil

class AddMedicationDialog : DialogFragment() {

    private var _binding: FragmentAddMedicationDialogBinding? = null
    private val binding get() = _binding!! // access only non-null _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMedicationDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeButton.setOnClickListener { dismiss() }
        WindowInsetsUtil.setupWindowInsets(binding.dialogContainer)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Tag for the dialog fragment
        const val TAG = "AddMedicationDialog"
    }
}