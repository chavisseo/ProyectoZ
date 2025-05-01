package com.example.proyectoz

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DialogMateriaAgregada : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Inflas el layout
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_materia_agregada, null)

        // Construyes el AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        // Botón Aceptar: cierra el diálogo
        view.findViewById<Button>(R.id.btnAceptar)
            .setOnClickListener { dismiss() }

        return dialog

    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // Al cerrar (Aceptar o BACK), volvemos al FragmentMenu
        requireActivity().supportFragmentManager.popBackStack()
    }
}
