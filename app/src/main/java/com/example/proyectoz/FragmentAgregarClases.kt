package com.example.proyectoz

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class FragmentAgregarClases : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var nombreEscuela: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agregar_clases, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val inputCarrera = view.findViewById<EditText>(R.id.inputCarrera)
        val inputSemestre  = view.findViewById<EditText>(R.id.inputSemestre)
        val inputGrupo = view.findViewById<EditText>(R.id.inputGrupo)
        val inputEscuela = view.findViewById<EditText>(R.id.inputEscuela)
        nombreEscuela = arguments?.getString("nombre")

        inputEscuela.text = Editable.Factory.getInstance().newEditable(nombreEscuela ?: "")

        val btnAgregarClase = view.findViewById<Button>(R.id.btnAgregarClase)
        btnAgregarClase.setOnClickListener {

            val carrera = inputCarrera.text.toString().trim()
            val semestre = inputSemestre.text.toString().trim()
            val grupo = inputGrupo.text.toString().trim()
            val escuela = inputEscuela.text.toString().trim()

            if(carrera.isNotEmpty() && semestre.isNotEmpty() && grupo.isNotEmpty()
                && escuela.isNotEmpty()){
                val clase = hashMapOf(
                    "carrera" to carrera,
                    "semestre" to semestre,
                    "grupo" to grupo,
                    "escuela" to escuela
                )

                db.collection("Clases")
                    .add(clase).addOnSuccessListener {
                        // Mostrar el diálogo usando el FragmentManager de la Activity
                        val dialog = DialogMateriaAgregada()
                        dialog.isCancelable = true
                        dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT)
                    }
            }else{
                Toast.makeText(requireContext(), "Ingresa todos los campos", Toast.LENGTH_SHORT)
            }
        }


    }
}