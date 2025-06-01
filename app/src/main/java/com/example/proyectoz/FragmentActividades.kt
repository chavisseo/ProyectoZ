package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class FragmentActividades: Fragment(){

    private lateinit var db: FirebaseFirestore
    private lateinit var container: LinearLayout
    private var claveMateria: String? = null
    private var temario: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_actividades, container, false)
        this.container = view.findViewById(R.id.containerActividades)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        claveMateria = arguments?.getString("clave")
        temario = arguments?.getString("temario")

        val flAgregar = view.findViewById<FrameLayout>(R.id.flAgregar)

        flAgregar.setOnClickListener {
            val bundle = Bundle().apply {
                putString("clave", claveMateria)
                putString("temario", temario)
            }

            val fragmentGenerarActividad = FragmentGenerarActividad()
            fragmentGenerarActividad.arguments = bundle

            // Ripple + navegación
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragmentGenerarActividad)
                .addToBackStack(null)
                .commit()
        }


    }
}