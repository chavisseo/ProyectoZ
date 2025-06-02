package com.example.proyectoz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUp : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var nombreUsuarioInput : EditText
    private lateinit var correoInput : EditText
    private lateinit var contrasenaInput : EditText
    private lateinit var progressBar : ProgressBar
    private lateinit var btnRegistrar : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Variables
        auth = FirebaseAuth.getInstance()
        nombreUsuarioInput = findViewById<EditText>(R.id.inputUsuario)
        correoInput = findViewById<EditText>(R.id.inputCorreo)
        contrasenaInput = findViewById<EditText>(R.id.inputContrasena)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvIniciarSesion = findViewById<TextView>(R.id.tvIniciaSesion)



        //Registrar usuario
        btnRegistrar.setOnClickListener(){
            val nombreUsuario = nombreUsuarioInput.text.toString().trim()
            val correo = correoInput.text.toString().trim()
            val contrasena = contrasenaInput.text.toString().trim()

            if(nombreUsuario.isNotEmpty() && correo.isNotEmpty() && contrasena.isNotEmpty()){
                btnRegistrar.isEnabled = false
                progressBar.visibility = View.VISIBLE
                registrarUsuario(nombreUsuario, correo, contrasena)
            }else{
                Toast.makeText(this, "Completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }

        tvIniciarSesion.setOnClickListener {
            irALogin()
        }

    }

    private fun registrarUsuario(nombre: String, correo: String, contrasena: String){
        auth.createUserWithEmailAndPassword(correo, contrasena)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                btnRegistrar.isEnabled = true

                if(task.isSuccessful){
                    val usuario = auth.currentUser
                    val perfil = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    usuario?.updateProfile(perfil)?.addOnCompleteListener{
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        nombreUsuarioInput.text.clear()
                        correoInput.text.clear()
                        contrasenaInput.text.clear()
                        startActivity(Intent(this, Menu::class.java))
                        finish()

                    }
                }else{
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun irALogin(){
        val intent = Intent(this, SignIn::class.java)
        startActivity(intent)
        finish()
    }
}