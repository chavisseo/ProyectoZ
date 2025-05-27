package com.example.proyectoz

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

private lateinit var auth: FirebaseAuth

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
     /*  val clasesCardView = findViewById<CardView>(R.id.cardClases)
        clasesCardView.setOnClickListener(){
            val intent = Intent(this, Materias::class.java)
            startActivity(intent)
        }*/
    }

    fun actualizarTextoInferior(nuevoTexto: String) {
        val label = findViewById<TextView>(R.id.textInferior)
        label.text = nuevoTexto
    }
}