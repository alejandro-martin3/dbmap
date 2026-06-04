package com.alejandromartin.dbmap

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class MedirFragment : Fragment(R.layout.fragment_medir) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resultadoTextView = view.findViewById<TextView>(R.id.textResultadoRuido)
        val botonMedir = view.findViewById<Button>(R.id.botonMedir)

        botonMedir.setOnClickListener {
            resultadoTextView.text = getString(R.string.medicion_pendiente)
        }
    }
}