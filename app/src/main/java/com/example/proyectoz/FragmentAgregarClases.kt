package com.example.proyectoz

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentAgregarClases : Fragment() {

    private lateinit var db: FirebaseFirestore
    private var nombreEscuela: String? = null
    val userId = FirebaseAuth.getInstance().currentUser?.uid

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
        //hola
        val inputEscuela = view.findViewById<EditText>(R.id.inputEscuela)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        nombreEscuela = arguments?.getString("nombre")

        inputEscuela.text = Editable.Factory.getInstance().newEditable(nombreEscuela ?: "")

        val btnAgregarClase = view.findViewById<Button>(R.id.btnAgregarClase)

        btnAgregarClase.setOnClickListener {
            val carrera = inputCarrera.text.toString().trim()
            val semestre = inputSemestre.text.toString().trim()
            val grupo = inputGrupo.text.toString().trim()
            val escuela = inputEscuela.text.toString().trim()

            if (carrera.isNotEmpty() && semestre.isNotEmpty() && grupo.isNotEmpty() && escuela.isNotEmpty()) {
                btnAgregarClase.isEnabled = false
                progressBar.visibility = View.VISIBLE

                // Verificar si ya existe una clase con la misma carrera en la misma escuela
                db.collection("Clases")
                    .whereEqualTo("carrera", carrera)
                    .whereEqualTo("escuela", escuela)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            progressBar.visibility = View.GONE
                            btnAgregarClase.isEnabled = true
                            Toast.makeText(requireContext(), "Ya existe esa carrera en la escuela indicada", Toast.LENGTH_SHORT).show()
                        } else {
                            val clase = hashMapOf(
                                "carrera" to carrera,
                                "semestre" to semestre,
                                "grupo" to grupo,
                                "escuela" to escuela,
                                "userId" to userId
                            )

                            db.collection("Clases")
                                .add(clase)
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE
                                    btnAgregarClase.isEnabled = true

                                    val dialog = DialogMateriaAgregada()
                                    dialog.isCancelable = true
                                    dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    btnAgregarClase.isEnabled = true
                                    Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        btnAgregarClase.isEnabled = true
                        Toast.makeText(requireContext(), "Error al validar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Ingresa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }



    }
}