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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragmentAgregarMateria : Fragment() {

    private lateinit var db: FirebaseFirestore
    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash")

    private val PICK_PDF_FILE = 2
    private var selectedPdfUri: Uri? = null
    private var nombreClase: String? = null
    private var archivoSeleccionado: Boolean? = false
    private val listaTemas = mutableListOf<String>()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
        }

        return inflater.inflate(R.layout.fragment_agregar_materia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val inputClave = view.findViewById<EditText>(R.id.inputClave)
        val inputNombre = view.findViewById<EditText>(R.id.inputNombreMateria)
        val inputSemestre = view.findViewById<EditText>(R.id.inputSemestre)
        val btnSeleccionarTemario = view.findViewById<Button>(R.id.btnSeleccionarTemario)
        val btnAgregarMateria = view.findViewById<Button>(R.id.btnAgregarMateria)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
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
            val uri = selectedPdfUri

            if (archivoSeleccionado == false) {
                Toast.makeText(requireContext(), "Selecciona un temario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (clave.isNotEmpty() && nombre.isNotEmpty() && semestre.isNotEmpty() && nombreClase != null) {
                btnSeleccionarTemario.isEnabled = false
                btnAgregarMateria.isEnabled = false
                progressBar.visibility = View.VISIBLE

                generarTemario(uri!!) {
                    if (listaTemas.isEmpty()) {
                        Toast.makeText(requireContext(), "El archivo ingresado no es un temario", Toast.LENGTH_SHORT).show()
                        btnSeleccionarTemario.isEnabled = true
                        btnAgregarMateria.isEnabled = true
                        progressBar.visibility = View.GONE
                        return@generarTemario
                    }

                    val hash = listaTemas.mapIndexed { index, elemento ->
                        (index + 1).toString() to elemento
                    }.toMap(HashMap())

                    hash.put("clave", clave)
                    hash.put("userId", userId ?: "null")

                    val materia = hashMapOf(
                        "clave" to clave,
                        "nombre" to nombre,
                        "semestre" to semestre,
                        "clase" to nombreClase,
                        "temario" to listaTemas.joinToString("\n"),
                        "userId" to userId
                    )

                    db.collection("Materias")
                        .add(materia).addOnSuccessListener {
                            db.collection("Temas")
                                .add(hash).addOnSuccessListener {
                                    btnSeleccionarTemario.isEnabled = true
                                    btnAgregarMateria.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    val dialog = DialogMateriaAgregada()
                                    dialog.isCancelable = true
                                    dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                                }
                                .addOnFailureListener {
                                    btnSeleccionarTemario.isEnabled = true
                                    btnAgregarMateria.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            btnSeleccionarTemario.isEnabled = true
                            btnAgregarMateria.isEnabled = true
                            progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(requireContext(), "Ingresa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun generarTemario(uri: Uri, onComplete: () -> Unit) {
        if (uri != null) {
            val contentResolver = requireContext().contentResolver
            var contador = 1
            listaTemas.clear()

            lifecycleScope.launch(Dispatchers.IO) {
                var localRespuesta: String
                do {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        inputStream.use { stream ->
                            val prompt = content {
                                inlineData(
                                    bytes = stream.readBytes(),
                                    mimeType = "application/pdf"
                                )
                                text("""
                                    A continuación, te enviaré el temario de una materia escolar. Tu tarea será:  

1. Leer con sumo cuidado todo el contenido del documento, revisando cada página de principio a fin.  
2. Asegúrate de identificar claramente si el contenido corresponde explícitamente a un temario escolar.  
3. Si es un temario, busca el tema número $contador y devuélvelo exactamente como aparece, incluyendo todos sus subtemas asociados.  
4. Es obligatorio incluir absolutamente toda la información del tema y subtemas correspondientes, sin importar si el contenido es breve, incompleto o está dividido entre páginas.  
5. Si el tema número $contador no existe, responde únicamente con la palabra 'alto' (sin comillas) y sin agregar ningún comentario adicional.  
6. No añadas ningún otro tipo de texto, resumen o explicación fuera del tema solicitado.  

🔍 Muy importante:  
- Los temas y subtemas suelen tener el siguiente formato:  
  - **Tema:** Un número seguido de punto y un título. Ejemplo: `3. Comunicación del protocolo de investigación`.  
  - **Subtema:** Número del tema seguido de punto, otro número y punto, y un título. Ejemplo: `3.1. Estructura formal del documento acorde a lineamientos establecidos`.  
- Debes **incluir solo** los temas y subtemas que forman parte del temario académico.  

🚫 **Excluye expresamente cualquier sección que no sea tema o subtema del temario.** No debes incluir lo siguiente, aunque esté numerado o parezca un tema:  
- Competencias a desarrollar (por ejemplo: `4. Competencia(s) a desarrollar`)  
- Competencias previas, competencias específicas, actividades, prácticas, proyectos, recursos, ejercicios o cualquier sección similar que no sea parte del listado de temas.  

✅ Tu tarea consiste exclusivamente en identificar y devolver el tema solicitado y todos sus subtemas correspondientes, sin omitir ningún detalle.  
✅ Si encuentras secciones numeradas como “4. Competencia(s) a desarrollar”, debes **excluirlas completamente**. Solo se deben incluir temas y subtemas del contenido académico formal del temario.  
✅ Si dudas si un contenido es tema o no, verifica si forma parte del listado estructurado de temas y subtemas. Si no es un tema o subtema académico, exclúyelo.

Tu análisis debe abarcar minuciosamente todas las páginas del documento para no omitir ningún contenido válido.

                                """)
                            }

                            try {
                                localRespuesta = model.generateContent(prompt).text ?: "error"
                                Log.d("Gemini", "$contador: $localRespuesta")
                            } catch (e: Exception) {
                                Log.e("Gemini", "Error al generar contenido", e)
                                localRespuesta = "error"
                            }

                            if (localRespuesta.trim() != "alto") {
                                listaTemas.add(localRespuesta)
                            }

                            contador++
                        }
                    } else {
                        localRespuesta = "alto"
                    }
                } while (localRespuesta.trim() != "alto")

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Los temarios se han leído correctamente", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }

        } else {
            Toast.makeText(requireContext(), "Selecciona un temario", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_FILE && resultCode == RESULT_OK) {
            data?.data.also { uri ->
                selectedPdfUri = uri
                Toast.makeText(requireContext(), "Archivo cargado correctamente", Toast.LENGTH_SHORT).show()
                archivoSeleccionado = true
            }
        }
    }
}
