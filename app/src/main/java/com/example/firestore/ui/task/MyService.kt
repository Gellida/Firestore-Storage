package com.example.firestore.ui.task

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.example.firestore.databinding.FragmentHomeBinding
import com.example.firestore.databinding.FragmentNewTaskBinding
import com.example.firestore.ui.dashboard.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

val tag = "MyServicee"

class MyService : Service() {
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var binding : FragmentNewTaskBinding

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "Entra al intent", Toast.LENGTH_LONG).show()

        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding = FragmentNewTaskBinding.inflate(LayoutInflater.from(this))
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
        Log.i("firebase","Entrando a función imagetofirebase")

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
                val title = binding.title.text.toString()
                val description = binding.desc.text.toString()
                val uri = uri.toString()

                val task = Task(title, description, uri)
                binding.title.setText("")
                binding.desc.setText("")
                binding.photoUrl.text = ""
                saveTask(task)

            }?.addOnFailureListener { exception ->
                Log.w(
                    tag,
                    "Error getting download URL",
                    exception
                )
            }
        }.addOnFailureListener { exception ->
            Log.w(tag, "Error uploading image", exception)
        }
    }

    private fun saveTask(task: Task) = CoroutineScope(Dispatchers.IO).launch {
        val TodoTaskRef = firebaseFirestore.collection("Todo")
        try {
            TodoTaskRef.add(task).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Task saved successfully!", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


}