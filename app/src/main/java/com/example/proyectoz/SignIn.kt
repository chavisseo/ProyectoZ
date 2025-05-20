package com.example.proyectoz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

private lateinit var auth: FirebaseAuth

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //variables
        auth = FirebaseAuth.getInstance()
        val correoInput = findViewById<EditText>(R.id.inputCorreo)
        val contrasenaInput = findViewById<EditText>(R.id.inputContrasena)
        val btnAceptar = findViewById<Button>(R.id.btn_accept)

        btnAceptar.setOnClickListener(){
            val correo = correoInput.text.toString().trim()
            val contrasena = contrasenaInput.text.toString().trim()

            if(correo.isNotEmpty() && contrasena.isNotEmpty()){
                iniciarSesion(correo, contrasena)
            }else{
                Toast.makeText(this,"Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesion(correo: String, contrasena: String){
        auth.signInWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    startActivity(Intent(this, Menu::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}