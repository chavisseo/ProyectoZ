package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class FragmentMaterias : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_materias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardAgregar = view.findViewById<CardView>(R.id.cardAgregar)

        cardAgregar.setOnClickListener(){
            val fragmentAgregarMateria = FragmentAgregarMateria()

            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, fragmentAgregarMateria)
                addToBackStack(null)
                commit()
            }
        }
    }
}