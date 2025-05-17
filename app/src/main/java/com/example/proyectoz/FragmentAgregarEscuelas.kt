package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class FragmentAgregarEscuelas : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_escuelas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAgregarEscuelas = view.findViewById<Button>(R.id.btnAgregarEscuela)
        btnAgregarEscuelas.setOnClickListener {
            // Mostrar el diálogo usando el FragmentManager de la Activity
            val dialog = DialogMateriaAgregada()
            dialog.isCancelable = true
            dialog.show(requireActivity().supportFragmentManager, "success_dialog")
        }
    }
}