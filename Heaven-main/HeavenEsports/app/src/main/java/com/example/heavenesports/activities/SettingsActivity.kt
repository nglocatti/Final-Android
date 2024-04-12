package com.example.heavenesports.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.example.heavenesports.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        // Reemplazar el fragmento de preferencias en el contenedor
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()

        // Habilitar el botón de "Atrás" en la barra de acción
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    // Clase interna que representa el fragmento de preferencias
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Cargar las preferencias desde el archivo XML
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}