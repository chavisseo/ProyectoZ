package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

class FragmentAgregarEscuelas : Fragment() {

    private lateinit var db: FirebaseFirestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var escuelaSeleccionada: DocumentSnapshot? = null // Guarda la escuela actual para edición

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

        val spinnerEscuela = view.findViewById<Spinner>(R.id.spinnerEscuela)
        val nombreInput = view.findViewById<EditText>(R.id.inputNombreEscuela)
        val paisInput = view.findViewById<EditText>(R.id.inputPais)
        val estadoInput = view.findViewById<EditText>(R.id.inputEstado)
        val municipioInput = view.findViewById<EditText>(R.id.inputMunicipio)
        val numAlumnosInput = view.findViewById<EditText>(R.id.inputNumeroAlumnos)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnAgregarEscuelas = view.findViewById<Button>(R.id.btnAgregarEscuela)

        // Spinners de nivel y tipo
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

        // ==========================
        // 🔹 Cargar escuelas del usuario
        // ==========================
        val listaEscuelas = mutableListOf("Agregar nueva escuela")
        val listaDocumentos = mutableListOf<DocumentSnapshot?>()

        db.collection("Escuelas")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    listaEscuelas.add(doc.getString("nombre") ?: "Sin nombre")
                    listaDocumentos.add(doc)
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listaEscuelas)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerEscuela.adapter = adapter
            }

        // ==========================
        // 🔹 Cuando seleccionas una escuela del spinner
        // ==========================
        spinnerEscuela.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    // Modo agregar nueva escuela
                    escuelaSeleccionada = null
                    nombreInput.setText("")
                    paisInput.setText("")
                    estadoInput.setText("")
                    municipioInput.setText("")
                    numAlumnosInput.setText("")
                    spinnerNivel.setSelection(0)
                    spinnerTipo.setSelection(0)
                    btnAgregarEscuelas.text = "Agregar escuela"
                } else {
                    // Modo editar
                    escuelaSeleccionada = listaDocumentos[position - 1]
                    val datos = escuelaSeleccionada?.data

                    nombreInput.setText(datos?.get("nombre")?.toString() ?: "")
                    paisInput.setText(datos?.get("pais")?.toString() ?: "")
                    estadoInput.setText(datos?.get("estado")?.toString() ?: "")
                    municipioInput.setText(datos?.get("municipio")?.toString() ?: "")
                    numAlumnosInput.setText(datos?.get("numeroAlumnos")?.toString() ?: "")

                    // Seleccionar los valores correctos en los spinners
                    (0 until spinnerNivel.count).forEach {
                        if (spinnerNivel.getItemAtPosition(it).toString() == datos?.get("nivel")) {
                            spinnerNivel.setSelection(it)
                        }
                    }
                    (0 until spinnerTipo.count).forEach {
                        if (spinnerTipo.getItemAtPosition(it).toString() == datos?.get("tipo")) {
                            spinnerTipo.setSelection(it)
                        }
                    }

                    btnAgregarEscuelas.text = "Guardar cambios"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ==========================
        // 🔹 Acción del botón (agregar o editar)
        // ==========================
        btnAgregarEscuelas.setOnClickListener {
            val nombre = nombreInput.text.toString().trim()
            val pais = paisInput.text.toString().trim()
            val estado = estadoInput.text.toString().trim()
            val municipio = municipioInput.text.toString().trim()
            val numeroAlumnos = numAlumnosInput.text.toString().trim()
            val nivel = spinnerNivel.selectedItem.toString()
            val tipo = spinnerTipo.selectedItem.toString()

            if (nombre.isEmpty() || pais.isEmpty() || estado.isEmpty() ||
                municipio.isEmpty() || numeroAlumnos.isEmpty()
            ) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnAgregarEscuelas.isEnabled = false

            val datos = hashMapOf(
                "nombre" to nombre,
                "pais" to pais,
                "estado" to estado,
                "municipio" to municipio,
                "numeroAlumnos" to numeroAlumnos,
                "nivel" to nivel,
                "tipo" to tipo,
                "userId" to userId
            )

            if (escuelaSeleccionada == null) {
                // ➕ Agregar nueva
                db.collection("Escuelas")
                    .add(datos)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        btnAgregarEscuelas.isEnabled = true
                        Toast.makeText(requireContext(), "Escuela agregada correctamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        btnAgregarEscuelas.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // ✏️ Editar existente
                escuelaSeleccionada?.reference?.update(datos as Map<String, Any>)
                    ?.addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        btnAgregarEscuelas.isEnabled = true
                        Toast.makeText(requireContext(), "Cambios guardados", Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener {
                        progressBar.visibility = View.GONE
                        btnAgregarEscuelas.isEnabled = true
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
