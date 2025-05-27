package com.example.proyectoz

import android.content.Context
import android.os.Bundle
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class FragmentMaterias : Fragment() {

    private var nombreCarrera: String? = null
    private lateinit var db: FirebaseFirestore
    private val listaNombres = mutableListOf<String>()
    private lateinit var container: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_materias, container, false)
        this.container = view.findViewById(R.id.containerMaterias)
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val flAgregar = view.findViewById<FrameLayout>(R.id.flAgregar)
        val flPerfil = view.findViewById<FrameLayout>(R.id.flPerfil)
        nombreCarrera = arguments?.getString("carrera")
        (activity as? Menu)?.actualizarTextoInferior("Te encuentras dentro de $nombreCarrera")

        //obtenerClases()
        obtenerClases()

        flAgregar.setOnClickListener {
            val bundle = Bundle().apply {
                putString("carrera", nombreCarrera)
            }

            val fragmentAgregarMateria = FragmentAgregarMateria()
            fragmentAgregarMateria.arguments = bundle

            // Ripple + navegación
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentAgregarMateria)
                .addToBackStack(null)
                .commit()
        }

        flPerfil.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentAgregarMateria())
                .addToBackStack(null)
                .commit()
        }
    }

    fun obtenerClases(){
        db.collection("Materias")
            .get()
            .addOnSuccessListener { result ->
                for (document in result){
                    val nombre = document.getString("nombre")
                    val clase = document.getString("clase")

                    if(clase!=null && clase == nombreCarrera
                        && nombre!= null && !listaNombres.contains(nombre)){
                        listaNombres.add(nombre)
                    }
                }

                //agregarTarjetasDinamicas
                agregarTarjetasDinamicas(listaNombres, requireContext(), container)
            }
    }

    fun agregarTarjetasDinamicas(nombres: List<String>, contexto: Context, container: LinearLayout){
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

            fila.addView(crearCard(contexto, nombres[i], imagen))

            //Segunda tarjeta (si existe)
            if(i + 1 < nombres.size){
                fila.addView(crearCard(contexto, nombres[i + 1], imagen))
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

    fun crearCard(context: android.content.Context, texto: String, imagenResId: Int): CardView {
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
       /* frameLayout.setOnClickListener {
            val bundle = Bundle().apply {
                putString("carrera", texto)
            }
            val fragmentMaterias = FragmentMaterias()
            fragmentMaterias.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentMaterias)
                .addToBackStack(null)
                .commit()
        }*/

        linear.addView(imageView)
        linear.addView(textView)
        frameLayout.addView(linear)
        cardView.addView(frameLayout)
        return cardView
    }

    fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
