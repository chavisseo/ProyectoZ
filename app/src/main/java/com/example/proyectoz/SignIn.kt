package com.example.proyectoz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

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

        auth = FirebaseAuth.getInstance()
        val correoInput = findViewById<EditText>(R.id.inputCorreo)
        val contrasenaInput = findViewById<EditText>(R.id.inputContrasena)
        val btnAceptar = findViewById<Button>(R.id.btn_accept)
        val tvRegistrate = findViewById<TextView>(R.id.tvRegistrate)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnAceptar.setOnClickListener {
            val correo = correoInput.text.toString().trim()
            val contrasena = contrasenaInput.text.toString().trim()

            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {
                btnAceptar.isEnabled = false  // Desactivar botón
                progressBar.visibility = View.VISIBLE  // Mostrar cargando

                auth.signInWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(this) { task ->
                        progressBar.visibility = View.GONE  // Ocultar cargando
                        btnAceptar.isEnabled = true  // Habilitar botón

                        if (task.isSuccessful) {
                            startActivity(Intent(this, Menu::class.java))
                            finish()
                        } else {
                            val errorMessage = when (val exception = task.exception) {
                                is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas"
                                is FirebaseNetworkException -> "Error de red. Inténtalo de nuevo"
                                else -> "Error: ${exception?.localizedMessage}"
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegistrate.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }
    }
}
