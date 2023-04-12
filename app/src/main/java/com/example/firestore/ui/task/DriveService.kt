package com.example.firestore.ui.task

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
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

private val TAG = "ASDASD"

class DriveService : Service() {
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var TaskBinding : FragmentNewTaskBinding
    private lateinit var imageUri: Uri

    override fun onCreate() {
        super.onCreate()
        TaskBinding = FragmentNewTaskBinding.inflate(LayoutInflater.from(this));
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
        val imagesRef = storageRef.child("drive/${imageUri.lastPathSegment}")
        val uploadTask = imagesRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "Imagen subida a Storage: ${taskSnapshot.metadata?.path}")

            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                Log.d(TAG, "URL: $uri")


                val title = TaskBinding.title.text.toString()
                val description = TaskBinding.desc.text.toString()
                val uri = uri.toString()

                val task = Task(title, description, uri)
                TaskBinding.title.setText("")
                TaskBinding.desc.setText("")
                TaskBinding.photoUrl.text = ""
                saveTask(task)

            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error uploading image", exception)
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