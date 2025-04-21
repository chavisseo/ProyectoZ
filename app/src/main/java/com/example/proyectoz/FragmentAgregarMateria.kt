package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class FragmentAgregarMateria : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_materia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAgregarMateria = view.findViewById<Button>(R.id.btnAgregarMateria)

        btnAgregarMateria.setOnClickListener(){
            val dialog = DialogMateriaAgregada()
            dialog.show(childFragmentManager, "success_dialog")
        }

    }

}