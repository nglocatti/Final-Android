package com.example.heavenesports.fragments

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.heavenesports.activities.HomeActivity
import com.example.heavenesports.activities.ProviderType
import com.example.heavenesports.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthFragment : Fragment() {
    private lateinit var loginView : View
    private lateinit var edtEmail : EditText
    private lateinit var edtPassword : EditText
    private lateinit var btnIngresar : Button
    private lateinit var btnRegistrar : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el diseño para este fragmento
        loginView = inflater.inflate(R.layout.fragment_auth, container, false)

        edtEmail = loginView.findViewById(R.id.edt_email)
        edtPassword = loginView.findViewById(R.id.edt_password)
        btnIngresar = loginView.findViewById(R.id.btn_ingresar)
        btnRegistrar = loginView.findViewById(R.id.btn_registrar)

        setup()

        return loginView
    }

    override fun onStart() {
        super.onStart()

        // Mostrar la vista de autenticación al iniciar el fragmento
        loginView.findViewById<View>(R.id.authLayout).visibility = View.VISIBLE
    }

    private fun setup(){
        btnRegistrar.setOnClickListener{
            if (edtEmail.text.isNotEmpty() && edtPassword.text.isNotEmpty()){
                // Crear un nuevo usuario en Firebase Authentication
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(edtEmail.text.toString(),
                edtPassword.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = FirebaseAuth.getInstance().currentUser
                        val uid = user?.uid

                        val userData = hashMapOf(
                            "URL_foto_perfil" to "1",
                            "tag" to "1",
                            "username" to "1"
                        )
                        if (uid != null) {
                            val database = FirebaseFirestore.getInstance()
                            val userDocumentRef = database.collection("usuarios").document(uid)

                            userDocumentRef.set(userData)
                                .addOnSuccessListener {
                                    // Registro exitoso y datos de usuario escritos en Firestore
                                    ProviderType.BASIC.showHome(user.email ?: "")
                                }
                                .addOnFailureListener { e ->
                                    // Error al escribir los datos en Firestore
                                    showAlert()
                                    Log.e(TAG, "Error writing user data: ${e.message}")
                                }
                        }


                        ProviderType.BASIC.showHome(it.result?.user?.email ?: "")

                    }
                    else
                    {
                        showAlert()
                    }
                }
            }
        }
        btnIngresar.setOnClickListener{
            if (edtEmail.text.isNotEmpty() && edtPassword.text.isNotEmpty()){
                // Iniciar sesión con un usuario existente en Firebase Authentication
                FirebaseAuth.getInstance().signInWithEmailAndPassword(edtEmail.text.toString(),
                    edtPassword.text.toString()).addOnCompleteListener {

                    if (it.isSuccessful){
                        ProviderType.BASIC.showHome(it.result?.user?.email ?: "")
                    }
                    else
                    {
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(loginView.context)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun ProviderType.showHome(email: String){
        val homeIntent = Intent(loginView.context, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", name)
        }
        startActivity(homeIntent)
    }
}