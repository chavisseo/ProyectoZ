package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore


class FragmentAgregarEscuelas : Fragment() {

    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_escuelas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val nombreInput = view.findViewById<EditText>(R.id.inputNombreEscuela)
        val paisInput = view.findViewById<EditText>(R.id.inputPais)
        val estadoInput = view.findViewById<EditText>(R.id.inputEstado)
        val municipioInput = view.findViewById<EditText>(R.id.inputMunicipio)

        val spinnerNivel = view.findViewById<Spinner>(R.id.spinnerNivel)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.niveles_escuela,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerNivel.adapter = adapter
        }

        val spinnerTipo = view.findViewById<Spinner>(R.id.spinnerTipo)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.tipos_escuela,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipo.adapter = adapter
        }

        val btnAgregarEscuelas = view.findViewById<Button>(R.id.btnAgregarEscuela)
        btnAgregarEscuelas.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val pais = paisInput.text.toString().trim()
            val estado = estadoInput.text.toString().trim()
            val municipio = municipioInput.text.toString().trim()
            val nivel = spinnerNivel.selectedItem.toString()
            val tipo = spinnerTipo.selectedItem.toString()

            if(nombre.isNotEmpty() && pais.isNotEmpty() && estado.isNotEmpty()
                && municipio.isNotEmpty() && nivel.isNotEmpty() && tipo.isNotEmpty()){
                //Crear el objeto
                val escuela = hashMapOf(
                    "nombre" to nombre,
                    "pais" to pais,
                    "estado" to estado,
                    "municipio" to municipio,
                    "nivel" to nivel,
                    "tipo" to tipo
                )

                db.collection("Escuelas")
                    .add(escuela).addOnSuccessListener {
                        // Mostrar el diálogo usando el FragmentManager de la Activity
                        val dialog = DialogMateriaAgregada()
                        dialog.isCancelable = true
                        dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                    }
                    .addOnFailureListener{
                        Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(requireContext(), "Ingresa todos los campos", Toast.LENGTH_SHORT).show()
            }

        }
    }
}