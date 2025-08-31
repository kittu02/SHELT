package com.example.shelt

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("About SHELT")
            .setMessage("Smart Helmet application. \nDevelopers: Kirtan Makwana & Prashant Sarvaiya.")

            .setPositiveButton(android.R.string.ok, null)
            .create()
    }
}


