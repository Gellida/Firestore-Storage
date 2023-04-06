package com.example.firestore.ui.dashboard

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.firestore.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

private val TAG = "MyServicee"

class DriveService : Service() {
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate() {
        super.onCreate()
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(this));
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "Drive service", Toast.LENGTH_LONG).show()

        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()

        val uri = intent?.getParcelableExtra<Uri>("imageUri")


        // Subir la imagen a Firebase
        uri?.let {
            uploadDriveToFirebase(it)
        }
        return START_NOT_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    private fun uploadDriveToFirebase(imageUri: Uri) {
        val imagesRef = storageRef.child("drive/${imageUri!!.lastPathSegment}")
        val uploadTask = imagesRef.putFile(imageUri!!)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "Imagen subida a Storage: ${taskSnapshot.metadata?.path}")
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                Log.d(TAG, "URL: $uri")

                //Se sube la imagen a Firestore
                val task = hashMapOf(
                    "title" to binding.titulo.text.toString(),
                    "description" to binding.descripcion.text.toString(),
                    "downloadUrl" to uri.toString()
                )
                val image = hashMapOf(
                    "title" to binding.titulo.text.toString(),
                    "description" to binding.descripcion.text.toString(),
                    "downloadUrl" to uri.toString()
                )

                firebaseFirestore.collection("Tasks").document(binding.titulo.text.toString())
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
                        Toast.makeText(applicationContext,"Guardado Images",Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }

            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error uploading image", exception)
        }
    }


}