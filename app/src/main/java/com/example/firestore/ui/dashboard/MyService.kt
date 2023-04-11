package com.example.firestore.ui.dashboard

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.firestore.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

private val TAG = "MyServicee"
class MyService : Service() {
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var binding : FragmentHomeBinding

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "Entra al intent", Toast.LENGTH_LONG).show()

        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()

        val uri = intent?.getParcelableExtra<Uri>("imageUri")


        // Subir la imagen a Firebase
        uri?.let {
            uploadImageToFirebase(it)
        }
        return START_NOT_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    private fun uploadImageToFirebase(uri: Uri) {
        // Crea una referencia al archivo en el almacenamiento de Firebase
        val imageRef = storageRef.child("camera/${UUID.randomUUID()}.jpg")

        // Sube la imagen a Firebase
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // La imagen se ha subido correctamente, obtén la URL de descarga
            val downloadUrl = taskSnapshot.metadata?.reference?.downloadUrl

            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                // Guarda la URL de descarga en Firestore
                val imageUrl = downloadUrl.toString()
                val task = hashMapOf(
                    "title" to binding.titulo.text.toString(),
                    "description" to binding.descripcion.text.toString(),
                    "downloadUrl" to imageUrl
                )
                val image = hashMapOf(
                    "title" to binding.titulo.text.toString(),
                    "description" to binding.descripcion.text.toString(),
                    "downloadUrl" to imageUrl
                )

                firebaseFirestore.collection("Tasks").document(System.currentTimeMillis().toString())
                    .set(task)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext,"Guardado exitosamente",Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
                firebaseFirestore.collection("Images").document(binding.titulo.text.toString()+System.currentTimeMillis())
                    .set(image)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext,"Guardado en images",Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }?.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting download URL", exception)
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error uploading image", exception)
        }
    }


}

