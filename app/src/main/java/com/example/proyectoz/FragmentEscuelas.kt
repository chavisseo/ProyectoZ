package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class FragmentEscuelas : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_escuelas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val flAgregar = view.findViewById<FrameLayout>(R.id.flAgregar)
        val flPerfil = view.findViewById<FrameLayout>(R.id.flPerfil)

        flAgregar.setOnClickListener {
            // Ripple + navegación
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentAgregarEscuelas())
                .addToBackStack(null)
                .commit()
        }

        flPerfil.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentAgregarEscuelas())
                .addToBackStack(null)
                .commit()
        }
    }
}
