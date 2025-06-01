package com.example.proyectoz

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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


}