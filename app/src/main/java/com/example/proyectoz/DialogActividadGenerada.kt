package com.example.proyectoz

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class DialogActividadGenerada : androidx.fragment.app.DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Inflas el layout
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_actividad_agregada, null)

        // Construyes el AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        //Boton aceptar: cierra el dialogo
        view.findViewById<Button>(R.id.btnAceptar)
            .setOnClickListener {
                dismiss()
            }

        return dialog

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        requireActivity().supportFragmentManager.popBackStack()
    }
}