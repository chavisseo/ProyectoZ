package com.example.proyectoz

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth



class Menu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //variables
        val textoBienvenida = findViewById<TextView>(R.id.textSuperior)
        val usuario = FirebaseAuth.getInstance().currentUser
        val imagen = findViewById<ImageButton>(R.id.imageButton)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        val tvEscuelas = findViewById<TextView>(R.id.tvEscuelas)
        val tvClases = findViewById<TextView>(R.id.tvClases)
        val tvMaterias = findViewById<TextView>(R.id.tvMaterias)



        if (savedInstanceState == null){
            val fragmentEscuelas = FragmentEscuelas()
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragmentEscuelas)
                .commit()
        }

        if(usuario != null){

            textoBienvenida.text = "Hola, ${usuario.displayName}"
        }else{
            textoBienvenida.text = "Hola, Perro"
        }

        imagen.setOnClickListener {
            val logoutFragment = FragmentLogout()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, logoutFragment)
                .addToBackStack(null) // opcional, para volver atrás
                .commit()
        }

        btnHome.setOnClickListener {
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentEscuelas())
                .commit()
        }

        tvClases.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentClases())
                .addToBackStack(null)
                .commit()
        }

        tvEscuelas.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentEscuelas())
                .addToBackStack(null)
                .commit()
        }

        tvMaterias.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentMaterias())
                .addToBackStack(null)
                .commit()
        }

    }

    fun actualizarTextoInferior(nuevoTexto: String) {
        val label = findViewById<TextView>(R.id.textInferior)
        label.text = nuevoTexto
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if(currentFragment is FragmentEscuelas){
            finish()
        }else{
            super.onBackPressed()
        }

    }

    fun updateMenuHighlight(current: String) {
        val escuelas = findViewById<TextView>(R.id.tvEscuelas)
        val clases = findViewById<TextView>(R.id.tvClases)
        val materias = findViewById<TextView>(R.id.tvMaterias)
        val actividades = findViewById<TextView>(R.id.tvActividades)

        val defaultBackground = ContextCompat.getDrawable(this, android.R.color.transparent)
        val selectedBackground = ContextCompat.getDrawable(this, R.drawable.rounded_button)
        val defaultTextColor = ContextCompat.getColor(this, R.color.black) // Pon tu color normal
        val selectedTextColor = ContextCompat.getColor(this, android.R.color.white)     // Color para seleccionado

        val items = listOf(escuelas, clases, materias, actividades)

        items.forEach {
            it.background = defaultBackground
            it.setTextColor(defaultTextColor)
        }

        when (current) {
            "Escuelas" -> {
                escuelas.background = selectedBackground
                escuelas.setTextColor(selectedTextColor)
            }
            "Clases" -> {
                clases.background = selectedBackground
                clases.setTextColor(selectedTextColor)
            }
            "Materias" -> {
                materias.background = selectedBackground
                materias.setTextColor(selectedTextColor)
            }
            "Actividades" -> {
                actividades.background = selectedBackground
                actividades.setTextColor(selectedTextColor)
            }
        }
    }



}