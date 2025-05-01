package com.example.proyectoz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class FragmentMenu : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos tu layout de menú
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Buscamos los 4 FrameLayout que envuelven cada tarjeta
        val flClases = view.findViewById<FrameLayout>(R.id.flClases)
        val flPerfil = view.findViewById<FrameLayout>(R.id.flPerfil)
        val flActividades = view.findViewById<FrameLayout>(R.id.flActividades)
        val flGrupos = view.findViewById<FrameLayout>(R.id.flGrupos)

        // Cada FrameLayout navega al fragment correspondiente
        flClases.setOnClickListener {
            navigateTo(FragmentMaterias())
        }
        flPerfil.setOnClickListener {
            navigateTo(FragmentMaterias())
        }
        flActividades.setOnClickListener {
            navigateTo(FragmentMaterias())
        }
        flGrupos.setOnClickListener {
            navigateTo(FragmentMaterias())
        }
    }

    // Método helper para hacer replace + backstack
    private fun navigateTo(fragment: Fragment) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
