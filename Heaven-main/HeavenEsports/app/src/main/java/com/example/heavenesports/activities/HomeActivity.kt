package com.example.heavenesports.activities

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.heavenesports.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType{
    BASIC,
}

class HomeActivity : AppCompatActivity() {

    private lateinit var  menubottom : BottomNavigationView
    private lateinit var navhost : NavHostFragment

    private lateinit var fotousercache : ImageView

    private val database = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inicializar los componentes de la interfaz de usuario
        navhost = supportFragmentManager.findFragmentById(R.id.home_nav) as NavHostFragment

        menubottom = findViewById(R.id.tb_bottom)

        fotousercache = findViewById(R.id.foto_user_cache)

        // Configurar la navegación con el controlador de navegación
        NavigationUI.setupWithNavController(menubottom, navhost.navController)

        // Configurar la barra de herramientas (toolbar)
        val toolbar = findViewById<Toolbar>(R.id.tb_home)
        setSupportActionBar(toolbar)

        // Establecer el título de la actividad
        title = "Inicio"

        // Obtener datos pasados desde la actividad anterior
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        // Guardar los datos en las preferencias compartidas
        val prefs = getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

    }

    override fun onStart() {
        super.onStart()

        // Obtener el UID del usuario actualmente autenticado
        val useruid = FirebaseAuth.getInstance().currentUser?.uid

        // Obtener las preferencias compartidas para guardar datos adicionales
        val prefs = getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE).edit()

        // Consultar la base de datos para obtener la URL de la foto de perfil del usuario
        database.collection("usuarios").document("$useruid").get()
            .addOnSuccessListener { dataSnapshot  ->
                if (dataSnapshot  != null){
                    // Cargar la foto de perfil utilizando Glide
                    Glide.with(this)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .load(dataSnapshot.getString("URL_foto_perfil"))
                        .placeholder(R.drawable.foto_user)
                        .error(R.drawable.foto_user)
                        .into(fotousercache)

                    prefs.putString("foto_perfil", "Si")
                    prefs.apply()
                }
            }
            .addOnFailureListener{
                prefs.putString("foto_perfil", "No")
                prefs.apply()
            }
    }
}

