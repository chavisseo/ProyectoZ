package com.example.proyectoz

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.sink
import org.commonmark.node.Text
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class FragmentGenerarActividad : Fragment(){

    private lateinit var db: FirebaseFirestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.0-flash")
    private var claveMateria: String? = null
    private var temario: String? = null
    private var promptAux: String? = null
    private var objetivo: String? = null
    private var instrucciones: String? = null
    private var recursos: String? = null
    private var rubrica: String? = null
    private var idDocumento: String? = null
    private var respuestaDef: String? = null
    private lateinit var btnGenerar: Button
    private lateinit var btnPDF: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGuardar: Button





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_generar_actividad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        claveMateria = arguments?.getString("clave")
        temario = arguments?.getString("temario")
        idDocumento = arguments?.getString("id")

        val inputActividad = view.findViewById<TextView>(R.id.inputActividad)

        btnGenerar = view.findViewById<Button>(R.id.btnGenerarActividad)
        btnPDF = view.findViewById<Button>(R.id.btnGenerarPDF)
        btnGuardar = view.findViewById<Button>(R.id.btnGuardarActividad)
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar)


        obtenerNombreMateria()

        if(idDocumento != null){
            obtenerTexto(inputActividad)
        }

        btnGenerar.setOnClickListener {
            if(promptAux != null && temario != null){
                generarActividades(inputActividad)
            }else{
                Toast.makeText(requireContext(), "Espera un momento", Toast.LENGTH_SHORT).show()
            }
        }

        btnPDF.setOnClickListener {

            val objetivo: String
            val instrucciones: String
            val recursos: String
            val rubrica: String

            if(this.objetivo != null && this.instrucciones != null
                && this.recursos != null && this.rubrica != null){
                objetivo = this.objetivo.toString()
                instrucciones = this.instrucciones.toString()
                recursos = this.recursos.toString()
                rubrica = this.rubrica.toString()

                val pdfFile = createTwoPagePdf(objetivo, instrucciones, recursos, rubrica)
                savePdfToDownloads(pdfFile)
            }else if(idDocumento != null){
                extraerSecciones2(inputActividad.text.toString())

                objetivo = this.objetivo.toString()
                instrucciones = this.instrucciones.toString()
                recursos = this.recursos.toString()
                rubrica = this.rubrica.toString()

                val pdfFile = createTwoPagePdf(objetivo, instrucciones, recursos, rubrica)
                savePdfToDownloads(pdfFile)
            }else{
                Toast.makeText(requireContext(), "Primero genera la actividad", Toast.LENGTH_SHORT).show()
            }


        }

        btnGuardar.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btnGenerar.isEnabled = false
            btnPDF.isEnabled = false
            btnGuardar.isEnabled = false

            val actividad = inputActividad.text.toString().trim()


            if(actividad.isNotEmpty()){

                if(idDocumento != null){
                    db.collection("Actividades").whereEqualTo("id", idDocumento)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty){
                                val docId = documents.documents[0].id
                                val actividadHash = hashMapOf<String, Any?>(
                                    "actividad" to respuestaDef,
                                    "temario" to temario,
                                    "clave" to claveMateria,
                                    "userId" to userId
                                )

                                db.collection("Actividades").document(docId).update(actividadHash)
                                    .addOnSuccessListener {
                                        progressBar.visibility = View.GONE
                                        btnGenerar.isEnabled = true
                                        btnPDF.isEnabled = true
                                        btnGuardar.isEnabled = true

                                        val dialog = DialogActividadGenerada()
                                        dialog.isCancelable = true
                                        dialog.show(requireActivity().supportFragmentManager, "success_dialog")
                                    }.addOnFailureListener {
                                        progressBar.visibility = View.GONE
                                        btnGenerar.isEnabled = true
                                        btnPDF.isEnabled = true
                                        btnGuardar.isEnabled = true
                                        Toast.makeText(requireContext(), "Ocurrio un error al guardar", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                }else{
                    val nuevaActividadRef = db.collection("Actividades").document()
                    val idGenerado = nuevaActividadRef.id

                    val actividadHash = hashMapOf(
                        "actividad" to respuestaDef,
                        "timestamp" to FieldValue.serverTimestamp(),
                        "temario" to temario,
                        "id" to idGenerado,
                        "clave" to claveMateria,
                        "userId" to userId
                    )

                    nuevaActividadRef.set(actividadHash)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            btnGenerar.isEnabled = true
                            btnPDF.isEnabled = true
                            btnGuardar.isEnabled = true
                            val dialog = DialogActividadGenerada()
                            dialog.isCancelable = true
                            dialog.show(requireActivity().supportFragmentManager, "success_dialog")

                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            btnGenerar.isEnabled = true
                            btnPDF.isEnabled = true
                            btnGuardar.isEnabled = true
                            Toast.makeText(requireContext(), "Ocurrio un error al guardar", Toast.LENGTH_SHORT).show()
                        }
                }



            }else{
                Toast.makeText(requireContext(), "No ha generado una actividad", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun generarActividades(textView: TextView){

        //Mostrar progressBar
        progressBar.visibility = View.VISIBLE
        btnGenerar.isEnabled = false
        btnPDF.isEnabled = false
        btnGuardar.isEnabled = false

        val prompt = content {
            text(
                """
        Te proporcionaré un temario. A partir de él, crea una actividad didáctica (puede ser práctica, teórica o interactiva) que me ayude a comprender mejor los conceptos.

        La actividad será $promptAux. Tu respuesta debe contener exactamente las siguientes secciones, nombradas estrictamente así (incluyendo símbolos):

        1. Objetivo de la actividad&&&
        &&&2. Instrucciones%%%
        %%%3. Recursos necesarios;;;
        ;;;4. Rúbrica de Evaluación:::

        Es muy importante que no modifiques los nombres de estas secciones, incluyendo los símbolos.

        Además, en la sección "Rúbrica de Evaluación:::", usa el siguiente formato:
        - Evita usar tablas.
        - Describe los criterios en forma de lista, indicando claramente cada criterio con un guion (-) o viñeta.
        - Cada criterio debe tener una breve descripción del aspecto a evaluar y su puntuación máxima posible (por ejemplo: "- Claridad en la presentación (10 puntos)").
        - Asegúrate de que sea legible y fácil de seguir.

        Importante:
        - La actividad no necesariamente debe abarcar todo el temario. Puede centrarse solo en uno o varios subtemas específicos. Esto ayudará a que las actividades sean más variadas y enfocadas.

        Adicionalmente:
        - No incluyas mensajes de bienvenida o despedida. 

        Aquí está el temario:
        $temario
        """.trimIndent()
            )
        }

        // Usar CoroutineScope para lanzar sin bloquear
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val respuesta = withContext(Dispatchers.IO) {
                    model.generateContent(prompt).text ?: "null"
                }

                Log.d("Gemini", "Respuesta: $respuesta")
                val markwon = Markwon.create(requireContext())
                markwon.setMarkdown(textView, respuesta)

                extraerSecciones2(respuesta)
                Log.d("prueba", "Objetivo: $objetivo")
                Log.d("prueba", "Instrucciones: $instrucciones")
                Log.d("prueba", "Recursos: $recursos")
                Log.d("prueba", "Rubrica: $rubrica")
                respuestaDef = respuesta

            } catch (e: Exception) {
                Log.e("Gemini", "Error al generar contenido", e)
                Toast.makeText(requireContext(), "Error al generar respuesta", Toast.LENGTH_SHORT).show()
            } finally {
                // Ocultar ProgressBar al terminar
                progressBar.visibility = View.GONE
                btnGenerar.isEnabled = true
                btnPDF.isEnabled = true
                btnGuardar.isEnabled = true
            }
        }
    }

    fun obtenerTexto(textView: TextView){
        db.collection("Actividades")
            .whereEqualTo("id",idDocumento)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    val textoObtenido = document.getString("actividad") ?: "No hay actividad"
                    val markwon = Markwon.create(requireContext())
                    markwon.setMarkdown(textView, textoObtenido)
                }
            }
    }

    fun obtenerNombreEscuela(escuela: String){
        db.collection("Escuelas")
            .whereEqualTo("nombre", escuela)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    val estado = document.getString("estado")
                    val municipio = document.getString("municipio")
                    val nivel = document.getString("nivel")
                    val pais = document.getString("pais")
                    val tipo = document.getString("tipo")
                    promptAux += "en la escuela $escuela, la cual es $tipo de nivel $nivel ubicada en $municipio, $estado, $pais. "
                    Log.d("prueba", "P:$promptAux")
                }
            }
    }

    fun obtenerNombreClase(clase: String){
        db.collection("Clases")
            .whereEqualTo("carrera", clase)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    val escuela = document.getString("escuela") ?: ""
                    obtenerNombreEscuela(escuela)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al obtener los documentos $exception", Toast.LENGTH_SHORT).show()
            }

    }

    fun obtenerNombreMateria(){
        db.collection("Materias").
                whereEqualTo("clave", claveMateria)
            .get()
            .addOnSuccessListener {
                documents ->
                for(document in documents){
                    val clase = document.getString("clase") ?: ""
                    val semestre = document.getString("semestre")
                    val nombre = document.getString("nombre")
                    promptAux = "para una materia $nombre en el semestre numero $semestre de $clase "
                    obtenerNombreClase(clase)
                    Log.d("prueba", "a $clase")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al obtener los documentos $exception", Toast.LENGTH_SHORT).show()
            }
    }


    fun extraerSecciones2(texto: String){
        objetivo = texto.substringAfter("&&&").substringBefore("&&&")
        instrucciones = texto.substringAfter("%%%").substringBefore("%%%")
        recursos = texto.substringAfter(";;;").substringBefore(";;;")
        rubrica = texto.substringAfter(":::")

    }

    fun createTwoPagePdf(objetivo: String, instrucciones: String,
                         recursos: String, rubrica: String): File{
        //1. Crear pdfDocument
        val document = PdfDocument()

        //2. Definir tamaños (A4)
        val pageWidth = 595
        val pageHeight = 842

        //3. Configurar paint para titulos y subtitulos
        val titlePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
        }

        val subtitlePaint = Paint().apply {
            textSize = 12f
        }

        //4. Pagina 1
        val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight,1).create()
        val page1 = document.startPage(pageInfo1)
        drawPageContent(page1.canvas, titlePaint, subtitlePaint,
            title = "Actividad",
            firstSub = "Objetivos",
            firstSubText = "$objetivo",
            secondSub = "Instrucciones",
            secondSubText = "$instrucciones",
            halfY = pageHeight / 2)
        document.finishPage(page1)

        //5. Pagina 2
        val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create()
        val page2 = document.startPage(pageInfo2)
        drawPageContent(page2.canvas, titlePaint, subtitlePaint,
            title = "Actividad",
            firstSub = "Recursos necesarios",
            firstSubText = "$recursos",
            secondSub = "Rubrica",
            secondSubText = "$rubrica",
            halfY = pageHeight)
        document.finishPage(page2)

        //6. Guardar el PDF en cache
        val pdfDir = File(requireContext().cacheDir, "pdfs").apply { if(!exists()) mkdirs() }
        val file = File(pdfDir, "actividad.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file

    }

    fun drawPageContent(
        canvas: Canvas,
        titlePaint: Paint,
        subtitlePaint: Paint,
        title: String,
        firstSub: String,
        firstSubText: String,
        secondSub: String,
        secondSubText: String,
        halfY: Int
    ){
        //Centrar el titulo
        val pageWidth = canvas.width
        val titleWidth = titlePaint.measureText(title)
        canvas.drawText(title, (pageWidth - titleWidth) / 2, 50f, titlePaint)


        // Primer subtítulo arriba a la izquierda
        val firstSubY = 100f
        canvas.drawText(firstSub, 20f, firstSubY, subtitlePaint)

        //Texto debajo del primer subtitulo
        val firstTextY = firstSubY + 5f
        val textPaint = Paint().apply { textSize = 11f }
        drawMultilineText(canvas, firstSubText, 20f, firstTextY, textPaint, pageWidth - 40f)

        // Segundo subtitulo
        val secondSubY = halfY.toFloat() / 2
        canvas.drawText(secondSub, 20f, secondSubY, subtitlePaint)

        //Texto debajo del segundo subtitulo
        val secondTextY = secondSubY + 5f
        drawMultilineText(canvas, secondSubText, 20f, secondTextY, textPaint, pageWidth - 40f)
    }

    fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        paint: Paint,
        maxWidth: Float,
        lineSpacingExtra: Float = 0f,
        lineSpacingMultiplier: Float = 1f
    ) {
        // Helper: convierte el texto con marcadores ** en un Spannable con StyleSpan(BOLD).
        fun parseBoldSpans(input: String): SpannableStringBuilder {
            val builder = SpannableStringBuilder()
            var i = 0
            val length = input.length

            while (i < length) {
                // Si encontramos dos asteriscos seguidos
                if (i + 1 < length && input[i] == '*' && input[i + 1] == '*') {
                    // Buscamos el siguiente par de asteriscos
                    val startBold = i + 2
                    var endBold = -1
                    var j = startBold
                    while (j + 1 < length) {
                        if (input[j] == '*' && input[j + 1] == '*') {
                            endBold = j
                            break
                        }
                        j++
                    }
                    if (endBold != -1) {
                        // Extraemos el texto interior
                        val boldText = input.substring(startBold, endBold)
                        val spanStart = builder.length
                        builder.append(boldText)
                        // Aplicamos negrita sobre [spanStart, spanStart + boldText.length)
                        builder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            spanStart,
                            spanStart + boldText.length,
                            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        // Avanzamos i más allá de los **
                        i = endBold + 2
                        continue
                    } else {
                        // Si no hay par de cierre, lo tomamos como texto normal
                        builder.append("**")
                        i += 2
                        continue
                    }
                }
                // Si no es el inicio de **, copiamos el carácter normal
                builder.append(input[i])
                i++
            }
            return builder
        }

        // Convertimos el texto crudo a Spannable (con negritas aplicadas donde corresponda).
        val spannable = parseBoldSpans(text)

        // StaticLayout requiere TextPaint, así que creamos uno a partir de Paint si hace falta:
        val textPaint = if (paint is TextPaint) paint else TextPaint(paint)

        // Construimos el StaticLayout según versión de API:
        val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder
                .obtain(spannable, 0, spannable.length, textPaint, maxWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                spannable,
                textPaint,
                maxWidth.toInt(),
                Layout.Alignment.ALIGN_NORMAL,
                lineSpacingMultiplier,
                lineSpacingExtra,
                false
            )
        }

        // Dibujamos el layout en el canvas desplazado a (x, startY):
        canvas.save()
        canvas.translate(x, startY)
        layout.draw(canvas)
        canvas.restore()
    }

    fun savePdfToDownloads(pdfFile: File) {
        val fileName = "actividad_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //Android 10 o superior
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
                put(MediaStore.Downloads.RELATIVE_PATH, "Download/MiCarpetaPDFs")
            }

            val resolver = requireContext().contentResolver
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val itemUri = resolver.insert(collection, contentValues)

            itemUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    pdfFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            //Android 9 o inferior
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1001
                )
                return
            }

            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outFile = File(downloadsDir, fileName)

            try {
                pdfFile.inputStream().use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(requireContext(), "PDF guardado en Descargas", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al guardar PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }


}