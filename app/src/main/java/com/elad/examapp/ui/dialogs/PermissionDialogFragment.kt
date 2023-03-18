package com.elad.examapp.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.elad.examapp.R
import com.elad.examapp.model.PermissionType

class PermissionDialogFragment(private val permissionType: PermissionType, private val onConfirm: () -> Unit) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.setCancelable(false)
        return AlertDialog.Builder(context)
            .setTitle(getString(R.string.permissions_title))
            .setMessage(
                when(permissionType) {
                    PermissionType.LOCATION -> getString(R.string.location_message)
                    PermissionType.BLUETOOTH -> getString(R.string.bluetooth_message)
                    else -> getString(R.string.permissions_message)
                }
            )
            .setPositiveButton(
                getString(R.string.ok)
            ) { dialog: DialogInterface, _ ->
                onConfirm.invoke()
                dialog.cancel()
            }
            .setNegativeButton(
                getString(R.string.cancel)
            ) { dialog: DialogInterface, _ -> dialog.cancel() }
            .create()
    }

    companion object {
        var TAG = "LocationDialogFragment"
    }
}