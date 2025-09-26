package com.example.proyectoz

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout

class FragmentTemarioManual : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_temario_manual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contenedorTemas = view.findViewById<LinearLayout>(R.id.contenedorTemas)
        val imgBtnAgregarTema = view.findViewById<ImageButton>(R.id.imgBtnAgregarTema)

        imgBtnAgregarTema.setOnClickListener {
            //Creamos un linearLayout horizontal para agrupar EditText e ImageButton
            val layoutItem = LinearLayout(this.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0,12,0,0)
                }
            }

            //Crear el editText

            val editText = EditText(this.context).apply {
                hint = "Escribe un tema"
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0,4,4,0)
                }
                setPadding(8,8,8,8)
                setBackgroundResource(R.drawable.rounded_edittxt)
                setTextColor(Color.BLACK)
                gravity = Gravity.START
            }

            //Crear el imageButton
            val imageButton = ImageButton(this.context).apply {
                setImageResource(R.drawable.baseline_check)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setBackgroundResource(android.R.color.transparent)
                contentDescription = "Confirmar tema"
            }

            //agregar el EditText y el imageButton al layout horizontal
            layoutItem.addView(editText)
            layoutItem.addView(imageButton)

            //Agregar el layoutItem al contenedor principal
            contenedorTemas.addView(layoutItem)
        }
    }
}