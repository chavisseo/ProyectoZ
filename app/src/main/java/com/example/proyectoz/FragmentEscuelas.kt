package com.example.proyectoz

import android.graphics.Typeface
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
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import com.google.api.Context
import com.google.firebase.firestore.FirebaseFirestore

class FragmentEscuelas : Fragment() {

    private lateinit var db : FirebaseFirestore
    private lateinit var container: LinearLayout
    private val listaNombres = mutableListOf<String>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_escuelas, container, false)
        db = FirebaseFirestore.getInstance()

        this.container = view.findViewById(R.id.containerEscuelas)
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val flAgregar = view.findViewById<FrameLayout>(R.id.flAgregar)
        val flPerfil = view.findViewById<FrameLayout>(R.id.flPerfil)

        obtenerEscuelas()
        //val container = view.findViewById<LinearLayout>(R.id.containerEscuelas)
        //agregarTarjetasDinamicas(listaNombres, requireContext(), container)
        flAgregar.setOnClickListener {
            // Ripple + navegación
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentAgregarEscuelas())
                .addToBackStack(null)
                .commit()
        }

        flPerfil.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentClases())
                .addToBackStack(null)
                .commit()
        }
    }

    fun obtenerEscuelas(){
        db.collection("Escuelas")
            .get()
            .addOnSuccessListener { result ->
                for (document in result){
                    val nombre = document.getString("nombre")
                    if(nombre != null && !listaNombres.contains(nombre)){
                        listaNombres.add(nombre)
                    }
                }
                agregarTarjetasDinamicas(listaNombres, requireContext(), container)
            }
    }

    fun agregarTarjetasDinamicas(nombres: List<String>, contexto: android.content.Context, container: LinearLayout){
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
