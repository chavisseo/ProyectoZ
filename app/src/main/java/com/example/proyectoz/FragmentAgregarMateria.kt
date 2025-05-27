package com.example.proyectoz

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class FragmentAgregarMateria : Fragment() {

    private lateinit var db: FirebaseFirestore
    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash")

    private val PICK_PDF_FILE = 2
    private var selectedPdfUri: Uri? = null
    private var respuesta: String? = null
    private var nombreClase: String? = null
    private var archivoSeleccionado: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (FirebaseApp.getApps(requireContext()).isEmpty()){
            FirebaseApp.initializeApp(requireContext())
        }

        return inflater.inflate(R.layout.fragment_agregar_materia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        //Variables para el manejo de archivos

        val inputClave = view.findViewById<EditText>(R.id.inputClave)
        val inputNombre = view.findViewById<EditText>(R.id.inputNombreMateria)
        val inputSemestre = view.findViewById<EditText>(R.id.inputSemestre)
        val btnSeleccionarTemario = view.findViewById<Button>(R.id.btnSeleccionarTemario)
        val btnAgregarMateria = view.findViewById<Button>(R.id.btnAgregarMateria)
        nombreClase = arguments?.getString("carrera")

        btnSeleccionarTemario.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            startActivityForResult(intent, PICK_PDF_FILE)
        }

        btnAgregarMateria.setOnClickListener {
            val clave = inputClave.text.toString().trim()
            val nombre = inputNombre.text.toString().trim()
            val semestre = inputSemestre.text.toString().trim()
            var response: String? = null

            var uri: Uri? = null
            uri = selectedPdfUri

            if(uri != null){
                generarTemario(uri)
                response = respuesta.toString().trim()
            }

            if(archivoSeleccionado == false){
                Toast.makeText(requireContext(), "Selecciona un temario",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(response == "null" && archivoSeleccionado == true){
                Toast.makeText(requireContext(), "Se está leyendo el temario, espere un momento",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(response == "1"){
                Toast.makeText(requireContext(), "El archivo ingresado no es un temario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(clave.isNotEmpty() && nombre.isNotEmpty() && semestre.isNotEmpty()
                && nombreClase!=null){
                //Crear el objeto
                val materia = hashMapOf(
                    "clave" to clave,
                    "nombre" to nombre,
                    "semestre" to semestre,
                    "clase" to nombreClase,
                    "temario" to response
                )

                db.collection("Materias")
                    .add(materia).addOnSuccessListener {
                        // Mostrar el diálogo usando el FragmentManager de la Activity
                        val dialog = DialogMateriaAgregada()
                        dialog.isCancelable = true
                        dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(requireContext(), "Ingresa todos los campos", Toast.LENGTH_SHORT).show()
            }

        }

    }

    fun generarTemario(uri: Uri){

        if(uri != null){
            val contentResolver = requireContext().applicationContext.contentResolver
            val inputStream = contentResolver.openInputStream(uri)

            if(inputStream != null){
                inputStream.use { stream ->
                    val prompt = content {
                        inlineData(
                            bytes = stream.readBytes(),
                            mimeType = "application/pdf"
                        )
                        text("Te compartiré el temario de una materia escolar, devuelveme en texto los temas y subtemas dentro del mismo." +
                                "No incluyas nada más, solo devuelveme los temas y subtemas." +
                                "Revisa bien el documento y asegurate de no dejar ningun tema o subtema fuera de tu respuesta, algunos pueden estar cortados al final de la pagina o al inicio" +
                                "En caso de que el contenido del archivo que te mande no incluya temas o subtemas devuelveme un '1' y nada más")
                    }

                    lifecycleScope.launch {
                        try{
                            respuesta = model.generateContent(prompt).text
                        }catch (e: Exception){
                            Log.e("Gemini", "Error al generar contenido", e)
                        }


                    }
                }
            }
        }else{
            Toast.makeText(requireContext(), "Selecciona un temario", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PDF_FILE && resultCode == RESULT_OK){
            data?.data.also { uri ->
                selectedPdfUri = uri
                Toast.makeText(requireContext(), "Archivo cargado correctamente", Toast.LENGTH_SHORT).show()
                archivoSeleccionado = true
            }
        }
    }


}
