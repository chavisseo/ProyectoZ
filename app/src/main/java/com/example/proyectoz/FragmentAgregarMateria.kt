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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking


class FragmentAgregarMateria : Fragment() {

    private lateinit var db: FirebaseFirestore
    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash")

    private val PICK_PDF_FILE = 2
    private var selectedPdfUri: Uri? = null
    private var respuesta: String? = null
    private var nombreClase: String? = null
    private var archivoSeleccionado: Boolean? = false
    private val listaTemas = mutableListOf<String>()

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
                && nombreClase!=null && listaTemas.isNotEmpty()){
                //Crear el objeto

                val hash = listaTemas.mapIndexed { index, elemento ->
                    (index + 1).toString() to elemento
                }.toMap(HashMap())

                hash.put("clave", clave)

                val materia = hashMapOf(
                    "clave" to clave,
                    "nombre" to nombre,
                    "semestre" to semestre,
                    "clase" to nombreClase,
                    "temario" to response
                )

                db.collection("Materias")
                    .add(materia).addOnSuccessListener {

                        db.collection("Temas")
                            .add(hash).addOnSuccessListener {
                                // Mostrar el diálogo usando el FragmentManager de la Activity
                                val dialog = DialogMateriaAgregada()
                                dialog.isCancelable = true
                                dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                            }

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
            var contador = 1

            do {
                val inputStream = contentResolver.openInputStream(uri)
                if(inputStream != null){


                    inputStream.use { stream ->
                        val prompt = content {
                            inlineData(
                                bytes = stream.readBytes(),
                                mimeType = "application/pdf"
                            )
                            text("A continuación, te enviaré el temario de una materia escolar. Tu tarea será extraer y devolver en formato de texto únicamente el tema número $contador junto con todos sus subtemas correspondientes." +
                                    "Asegúrate de no omitir ningún tema o subtema, incluso si estos se encuentran cortados al inicio o final de página del documento." +
                                    "Si el contenido del archivo no incluye temas o subtemas, responde exclusivamente con '1'." +
                                    "Si el tema número $contador no existe, responde exclusivamente con 'alto'." +
                                    "No incluyas ninguna otra información en tu respuesta.")
                        }

                        runBlocking {
                            try {
                                respuesta = model.generateContent(prompt).text
                            } catch (e: Exception) {
                                Log.e("Gemini", "Error al generar contenido", e)
                                respuesta = "error"
                            }
                        }

                        if(respuesta.toString().trim() != "alto"){
                            listaTemas.add(respuesta.toString())
                        }

                       /*Log.d("prueba", respuesta.toString())
                        if(respuesta.toString().contains("alto")){
                            Log.d("prueba","CAAA")
                        }*/
                        contador++
                    }

                }
            }while(respuesta.toString().trim() != "alto") // fin del for
            Toast.makeText(requireContext(), "Los temarios se han leido correctamente", Toast.LENGTH_SHORT).show()
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
