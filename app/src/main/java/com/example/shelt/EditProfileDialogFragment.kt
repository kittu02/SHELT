package com.example.shelt

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditProfileDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val prefs = requireContext().getSharedPreferences("user", android.content.Context.MODE_PRIVATE)
        view.findViewById<EditText>(R.id.etName).setText(prefs.getString("name", ""))
        view.findViewById<EditText>(R.id.etEmail).setText(prefs.getString("email", ""))
        view.findViewById<EditText>(R.id.etPhone).setText(prefs.getString("phone", ""))
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit()
                    .putString("name", view.findViewById<EditText>(R.id.etName).text.toString())
                    .putString("email", view.findViewById<EditText>(R.id.etEmail).text.toString())
                    .putString("phone", view.findViewById<EditText>(R.id.etPhone).text.toString())
                    .apply()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}


