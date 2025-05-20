package com.example.proyectoz

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

        if (savedInstanceState == null){
            val fragmentEscuelas = FragmentEscuelas()
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragmentEscuelas)
                .commit()
        }

     /*  val clasesCardView = findViewById<CardView>(R.id.cardClases)
        clasesCardView.setOnClickListener(){
            val intent = Intent(this, Materias::class.java)
            startActivity(intent)
        }*/
    }
}