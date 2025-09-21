package com.example.proyectoz

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.plus

class FragmentActividades: Fragment(){

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var db: FirebaseFirestore
    private lateinit var container: LinearLayout
    private var claveMateria: String? = null
    private var temario: String? = null
    private var nombreEscuela: String? = null
    private var nombreMateria: String? = null
    private val listaNumeros = mutableListOf<String>()
    private val listaNombres = mutableListOf<String>()
    private val listaActividades = mutableListOf<String>()
    private var objetivo: String? = null
    private var instrucciones: String? = null
    private var recursos: String? = null
    private var rubrica: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_actividades, container, false)
        this.container = view.findViewById(R.id.containerActividades)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        claveMateria = arguments?.getString("clave")
        temario = arguments?.getString("temario")

        obtenerActividades()
        obtenerNombreMateria()

        val flAgregar = view.findViewById<FrameLayout>(R.id.flAgregar)
        val flGenerarPdf = view.findViewById<FrameLayout>(R.id.flGenerarPdf)

        flAgregar.setOnClickListener {
            val bundle = Bundle().apply {
                putString("clave", claveMateria)
                putString("temario", temario)
            }

            val fragmentGenerarActividad = FragmentGenerarActividad()
            fragmentGenerarActividad.arguments = bundle

            // Ripple + navegación
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentGenerarActividad)
                .addToBackStack(null)
                .commit()
        }

        flGenerarPdf.setOnClickListener {
            if(listaActividades.isNotEmpty()){
                if(nombreEscuela != null && nombreMateria != null){
                    val pdfFile = createTwoPagePdf(listaActividades)
                    savePdfToDownloads(pdfFile)
                }else{
                    Toast.makeText(requireContext(), "Ocurrió un error inesperado", Toast.LENGTH_SHORT).show()
                }

            }else{
                Toast.makeText(requireContext(), "No hay actividades generadas para este tema",
                    Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun obtenerActividades(){
        db.collection("Actividades")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                var contador = 1;
                for (document in result){
                    Log.d("actividades", "Entro")
                    val clave = document.getString("clave")
                    val id = document.getString("id") ?: "1"
                    val temario  = document.getString("temario")
                    val actividad = document.getString("actividad")


                    if(clave != null && clave == claveMateria
                        && id != null && !listaNombres.contains(id)
                        && temario != null && temario == this.temario
                        && actividad != null){
                        listaNumeros.add("Actividad $contador")
                        listaNombres.add(id)
                        listaActividades.add(actividad)
                        contador++
                    }
                }

                if(isAdded){
                    //TarjetasDinamicas
                    agregarTarjetasDinamicas(listaNumeros,requireContext(), container, listaNombres)
                }
            }
    }

    fun extraerSecciones2(texto: String){
        objetivo = texto.substringAfter("&&&").substringBefore("&&&")
        instrucciones = texto.substringAfter("%%%").substringBefore("%%%")
        recursos = texto.substringAfter(";;;").substringBefore(";;;")
        rubrica = texto.substringAfter(":::")

    }

    fun agregarTarjetasDinamicas(nombres: List<String>, contexto: Context, container: LinearLayout, temas: List<String>){
        val imagen = R.drawable.clasesbtn

        for(i in nombres.indices step 2){
            //Crear nueva fila
            val fila = LinearLayout(contexto).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout. LayoutParams.WRAP_CONTENT
                ).apply {
                    setPadding(8.dpToPx(contexto),8.dpToPx(contexto),8.dpToPx(contexto),8.dpToPx(contexto))
                }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            fila.addView(crearCard(contexto, nombres[i], imagen, temas[i]))

            //Segunda tarjeta (si existe)
            if(i + 1 < nombres.size){
                fila.addView(crearCard(contexto, nombres[i + 1], imagen, temas[i + 1]))
            }else{
                val espacio = Space(contexto).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        150.dpToPx(contexto)).apply {
                        weight = 1f
                        marginStart = 4.dpToPx(contexto)
                    }
                }
                fila.addView(espacio)
            }

            container.addView(fila)
        }

    }

    fun crearCard(context: android.content.Context, texto: String, imagenResId: Int, temas: String): CardView {
        val heightInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            150f,
            context.resources.displayMetrics
        ).toInt()

        val radiusInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            16f,
            context.resources.displayMetrics
        )

        val cardView = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, heightInPx).apply {
                weight = 1f
                marginStart = 4.dpToPx(context)
                marginEnd = 4.dpToPx(context)
            }
            radius = radiusInPx
            cardElevation = 8f
            useCompatPadding = true
        }

        val frameLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            isClickable = true
            isFocusable = true
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            foreground = ContextCompat.getDrawable(context, outValue.resourceId)
        }

        val linear = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(60.dpToPx(context), 60.dpToPx(context))
            setImageResource(imagenResId)
            contentDescription = texto
        }

        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8.dpToPx(context)
            }
            text = texto
            textSize = 16f
        }

        //On click que mandará a la siguiente vista
        frameLayout.setOnClickListener {
            val bundle = Bundle().apply {
                putString("clave", claveMateria)
                putString("id", temas) //Aqui se manda el id de cada documento
                putString("temario", temario)
            }
            val fragmentGenerarActividad = FragmentGenerarActividad()
            fragmentGenerarActividad.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentGenerarActividad)
                .addToBackStack(null)
                .commit()
        }

        linear.addView(imageView)
        linear.addView(textView)
        frameLayout.addView(linear)
        cardView.addView(frameLayout)
        return cardView
    }

    fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }

    fun createTwoPagePdf(listaActividades: List<String>): File{
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

        val pageCover = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageCover)

        val canvas = page.canvas
        val paint = Paint()

        drawCoverPage(canvas, paint, pageCover.pageWidth, pageCover.pageHeight)
        document.finishPage(page)

        var contador = 1
        for(actividad in listaActividades){

            extraerSecciones2(listaActividades.get(contador - 1))

            val numeroPagina = (contador - 1) * 2 + 1

            //4. Pagina 1
            val pageInfo1 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight,numeroPagina).create()
            val page1 = document.startPage(pageInfo1)
            drawPageContent(page1.canvas, titlePaint, subtitlePaint,
                title = "Actividad $contador",
                firstSub = "Objetivos",
                firstSubText = "$objetivo",
                secondSub = "Instrucciones",
                secondSubText = "$instrucciones",
                halfY = pageHeight / 2)
            document.finishPage(page1)

            //5. Pagina 2
            val pageInfo2 = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, numeroPagina + 1 ).create()
            val page2 = document.startPage(pageInfo2)
            drawPageContent(page2.canvas, titlePaint, subtitlePaint,
                title = "Actividad $contador",
                firstSub = "Recursos necesarios",
                firstSubText = "$recursos",
                secondSub = "Rubrica",
                secondSubText = "$rubrica",
                halfY = pageHeight)
            document.finishPage(page2)

            contador++
        }



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

    private fun drawCoverPage(canvas: Canvas, paint: Paint, pageWidth: Int, pageHeight: Int) {
        // Configura el estilo del texto
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f

        val centerX = pageWidth / 2

        // Título principal
        canvas.drawText("Actividades para plan de trabajo", centerX.toFloat(), 200f, paint)

        // Resto de la información
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        canvas.drawText("$nombreEscuela", centerX.toFloat(), 300f, paint)
        canvas.drawText("$nombreMateria", centerX.toFloat(), 350f, paint)
        // Obtener fecha actual formateada
        val currentDate = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES")).format(Date())
        canvas.drawText(currentDate, centerX.toFloat(), 400f, paint)
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

    fun obtenerNombreEscuela(escuela: String){
        db.collection("Escuelas")
            .whereEqualTo("nombre", escuela)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    nombreEscuela = document.getString("nombre")


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
                    nombreMateria = document.getString("nombre") ?: ""

                    obtenerNombreClase(clase)

                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error al obtener los documentos $exception", Toast.LENGTH_SHORT).show()
            }
    }
}