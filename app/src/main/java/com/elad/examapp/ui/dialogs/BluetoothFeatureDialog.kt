package com.elad.examapp.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.elad.examapp.R

class BluetoothFeatureDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.please_enable_bluetooth_feature))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                dismiss()
            }
            .create()
    }

    companion object {
        const val TAG = "BluetoothFeatureDialog"
    }
}