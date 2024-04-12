package com.example.heavenesports.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.heavenesports.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.*

@Suppress("DEPRECATION")
class UserInfoFragment : Fragment() {

    private lateinit var userinfoView : View
    private lateinit var btnguardauser : Button
    private lateinit var btnregresar : Button
    private lateinit var edtusername : EditText
    private lateinit var edttag : EditText
    private lateinit var fotoperfil : CircleImageView
    private lateinit var textPlaceholder : TextView
    private val database = FirebaseFirestore.getInstance()
    private var flagfoto = 0
    private var fotouri: Uri? = null
    private lateinit var fotoURL : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el diseño del fragmento
        userinfoView = inflater.inflate(R.layout.fragment_user_info, container, false)

        textPlaceholder = userinfoView.findViewById(R.id.textPlaceholder)

        // Recupera el estado de la foto de perfil del SharedPreferences
        val prefs = activity?.getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE)
        if (prefs != null) {
            if (prefs.getString("foto_perfil","No") == "Si"){
                requireActivity().findViewById<View>(R.id.foto_user_cache) as ImageView
            }
        }

        btnguardauser = userinfoView.findViewById(R.id.btn_guardar_user)
        btnregresar = userinfoView.findViewById(R.id.btn_regresar)
        fotoperfil = userinfoView.findViewById<ImageView>(R.id.foto_perfil) as CircleImageView
        edtusername = userinfoView.findViewById(R.id.edt_username)
        edttag = userinfoView.findViewById(R.id.edt_tag)

        return userinfoView
    }

    override fun onStart() {
        super.onStart()

        val useruid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseStorage.getInstance().getReference("/fotos/player/$useruid")

        updateImageProfile()

        // Recupera el estado de la foto de perfil del SharedPreferences
        val prefs = activity?.getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE)
        if (prefs != null) {
            if (prefs.getString("foto_perfil","No") == "Si"){
                val fotouser =
                    requireActivity().findViewById<View>(R.id.foto_user_cache) as ImageView

                if (flagfoto == 0){
                    fotoperfil.setImageBitmap(fotouser.drawable.toBitmap())
                }
            }
            else {
                updateImageProfile()
            }
        }

        // Obtiene los datos del usuario de la base de datos de Firebase
        database.collection("usuarios").document("$useruid").get()
            .addOnSuccessListener { dataSnapshot  ->
                if (dataSnapshot  != null){
                    Log.d("Test", "DocumentSnapshot data: ${dataSnapshot["tag"].toString()}")
                    edttag.setText(dataSnapshot.getString("tag"))
                    edtusername.setText(dataSnapshot.getString("username"))
                }
            }

        // Configura el click listener para la foto de perfil
        fotoperfil.setOnClickListener{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(intent, 0)
            fotoperfil.background = null
            textPlaceholder.isVisible = true
        }

        // Configura el click listener para el botón "Guardar"
        btnguardauser.setOnClickListener{
            uploadImageToFirebaseStorage()
            saveUserToFirebaseDatabase(edtusername.text.toString(), edttag.text.toString())
        }

        // Configura el click listener para el botón "Regresar"
        btnregresar.setOnClickListener{
            flagfoto = 0
            val action =
                UserInfoFragmentDirections.actionUserInfoFragmentToHomeFragment()
            userinfoView.findNavController().navigate(action)
        }
    }

    // Sobrescribe el método onActivityResult para obtener la foto seleccionada
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!= null){

            flagfoto = 1
            fotouri = data.data
            try {
                fotouri?.let {
                    if(Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            userinfoView.context.contentResolver,
                            fotouri
                        )
                        fotoperfil.setImageBitmap(bitmap)
                    } else {
                        val source = ImageDecoder.createSource(userinfoView.context.contentResolver,
                            fotouri!!
                        )
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        fotoperfil.setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Sube la imagen seleccionada a Firebase Storage
    private fun uploadImageToFirebaseStorage(){
        if (fotouri == null)
        {
            return
        }

        val useruid = FirebaseAuth.getInstance().currentUser?.uid
        val ref = FirebaseStorage.getInstance().getReference("/fotos/player/$useruid")

        ref.putFile(fotouri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    fotoURL = it.toString()
                    //saveUserToFirebaseDatabase(fotoURL, edtusername.text.toString(), edttag.text.toString())
                    saveUserToFirebaseDatabase(edtusername.text.toString(), edttag.text.toString(),fotoURL)
                }
            }
            .addOnFailureListener{
                Log.e("FirebaseStorage", "Error al cargar la imagen")
                val errorMessage = "Error"
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
    }

    // Sobrescribe la función saveUserToFirebaseDatabase con parámetro opcional URL_foto
    @SuppressLint("ShowToast")
    fun saveUserToFirebaseDatabase(username: String, tag: String, URL_foto: String? = null) {
        val useruid = FirebaseAuth.getInstance().currentUser?.uid

        database.collection("usuarios").document(useruid!!).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Obtiene el URL_foto previo del documento
                    val previousPhotoURL = documentSnapshot.getString("URL_foto_perfil")

                    // Crea el HashMap con los datos del usuario, incluyendo el URL_foto_perfil previo si no se proporciona uno nuevo
                    val usuario = hashMapOf(
                        "URL_foto_perfil" to (URL_foto ?: previousPhotoURL),
                        "username" to username,
                        "tag" to tag
                    )

                    database.collection("usuarios").document(useruid).set(usuario)
                        .addOnSuccessListener {
                            showConfirmationDialog("¡Datos actualizados!", "Los datos se han actualizado correctamente.")
                        }
                        .addOnFailureListener {
                            Snackbar.make(userinfoView, R.string.error_foto, Snackbar.LENGTH_SHORT).setAction("Reintentar") {
                                database.collection("usuarios").add(usuario)
                            }
                        }
                } else {
                    val errorMessage = "Error en la carga de usuario"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Maneja cualquier error de consulta aquí
                Log.e("FirestoreQuery", "Error al consultar el documento del usuario: $exception")
            }
    }

/*

    // Versión sobrecargada sin URL_foto
    @SuppressLint("ShowToast")
    fun saveUserToFirebaseDatabase(username: String, tag: String) {
        val useruid = FirebaseAuth.getInstance().currentUser?.uid
        val usuario = hashMapOf(
            "username" to username,
            "tag" to tag
        )

        database.collection("usuarios").document(useruid!!).set(usuario)
            .addOnSuccessListener {
                database.collection("usuarios")
                    .add(usuario)
                showConfirmationDialog("¡Datos actualizados!", "Los datos se han actualizado correctamente.")
            }
            .addOnFailureListener{
                Snackbar.make(userinfoView,
                    R.string.error_foto, Snackbar.LENGTH_SHORT).setAction("Reintentar") {
                    database.collection("usuarios")
                        .add(usuario)
                }
            }
    }
    // Guarda los datos del usuario en la base de datos de Firebase
    @SuppressLint("ShowToast")
    fun saveUserToFirebaseDatabase(URL_foto : String, username : String, tag : String) {
        val useruid = FirebaseAuth.getInstance().currentUser?.uid
        val usuario = hashMapOf(
           "URL_foto_perfil" to URL_foto,
            "username" to username,
            "tag" to tag
        )

        database.collection("usuarios").document(useruid!!).set(usuario)
            .addOnSuccessListener {
                database.collection("usuarios")
                    .add(usuario)
                showConfirmationDialog("¡Datos actualizados!", "Los datos se han actualizado correctamente.")
                //Snackbar.make(userinfoView,
                //    R.string.ok_foto, Snackbar.LENGTH_SHORT).show()
        }
            .addOnFailureListener{
                Snackbar.make(userinfoView,
                    R.string.error_foto, Snackbar.LENGTH_SHORT).setAction("Reintentar"){
                    database.collection("usuarios")
                        .add(usuario)
                }
            }
    }
*/

    private fun showConfirmationDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    // Actualiza la imagen de perfil y muestra mensajes de éxito o error
    @SuppressLint("CommitPrefEdits", "UseCompatLoadingForDrawables")
    fun updateImageProfile(){
        val useruid = FirebaseAuth.getInstance().currentUser?.uid
        val prefs = activity?.getSharedPreferences(getString(R.string.login_pref), Context.MODE_PRIVATE)

        database.collection("usuarios").document("$useruid").get()
        .addOnSuccessListener { dataSnapshot  ->
            if (dataSnapshot  != null){
                if (prefs != null) {
                    prefs.edit().putString("foto_perfil", "Si")
                    fotoperfil.background = null
                    textPlaceholder.isVisible = true
                }
                prefs?.edit()?.apply()

                Snackbar.make(userinfoView,"Perfil cargado correctamente",Snackbar.LENGTH_SHORT).show()
            }
        }
        .addOnFailureListener{
            if (prefs != null) {
                prefs.edit().putString("foto_perfil", "No")
                fotoperfil.background = resources.getDrawable(R.drawable.foto_user)
                textPlaceholder.isVisible = true
            }
            prefs?.edit()?.apply()
            Snackbar.make(userinfoView,"Fallo la actualización de perfil",Snackbar.LENGTH_SHORT).show()
        }
    }
}