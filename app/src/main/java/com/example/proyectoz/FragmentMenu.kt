package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class FragmentMenu : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardClases = view.findViewById<CardView>(R.id.cardClases)

        cardClases.setOnClickListener(){
            val fragmentMaterias = FragmentMaterias()

            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragmentContainer, fragmentMaterias)
                addToBackStack(null)
                commit()
            }
        }
    }
}