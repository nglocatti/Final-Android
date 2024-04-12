package com.example.heavenesports.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import com.example.heavenesports.databinding.ActivityAuthBinding

// Clase que representa la actividad de autenticación
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla el diseño utilizando View Binding
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Establece el título de la actividad
        title = "Autenticación"

    }

    override fun onStart() {
        super.onStart()

        // Hace visible el layout de autenticación
        binding.authLayout.visibility = View.VISIBLE
    }

}


