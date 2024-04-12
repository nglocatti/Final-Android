package com.example.heavenesports.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.heavenesports.R
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val splashtimeout : Long = 3000 // Duración del splash en milisegundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Crear un Job principal para administrar las corrutinas
        val parentJob = Job()

        // Crear un ámbito de corrutina en el hilo principal
        val scope = CoroutineScope(Dispatchers.Main + parentJob)

        // Lanzar una corrutina para realizar un retraso y luego iniciar la sesión
        scope.launch {
            delay(splashtimeout) // Retraso para mostrar la pantalla de bienvenida
            session() // Iniciar la sesión después del retraso
        }
    }

    private fun session() {
        // Obtener las preferencias compartidas para obtener el correo electrónico y el proveedor de inicio de sesión
        val prefs = getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE)
        val email = prefs?.getString("email", null)
        val provider = prefs?.getString("provider", null)

        if(email != null && provider != null){
            // Si el correo electrónico y el proveedor están presentes, mostrar la pantalla principal
            showHome(email, ProviderType.valueOf(provider))
            finish()
        }
        else{
            // Si no hay datos de inicio de sesión, mostrar la pantalla de autenticación
            showAuth()
            finish()
        }
    }

    private fun showHome(email: String, provider: ProviderType){
        // Crear un Intent para la actividad de inicio y pasar los datos de inicio de sesión
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    private fun showAuth(){
        // Crear un Intent para la actividad de autenticación y comenzarla
        val authIntent = Intent(this, AuthActivity::class.java)
        startActivity(authIntent)
    }
}