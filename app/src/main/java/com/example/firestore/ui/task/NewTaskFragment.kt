package com.example.firestore.ui.task

import android.app.appsearch.AppSearchResult.RESULT_OK
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.firestore.databinding.FragmentNewTaskBinding
import com.example.firestore.ui.dashboard.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*

const val REQUEST_IMAGE_CAPTURE = 1;
class NewTaskFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNewTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var firebaseFirestore : FirebaseFirestore
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var photoLauncher: ActivityResultLauncher<Intent>
    private var imageUri: Uri? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity).get(TaskViewModel::class.java)

        binding.btnPhoto.setOnClickListener {
            resultLauncher.launch("image/*")
        }
        binding.btnSave.setOnClickListener{
            if (imageUri != null) {
                uploadImageDriveToStorage()
            } else {
                taskViewModel.title.value = binding.title.text.toString()
                taskViewModel.desc.value = binding.desc.text.toString()
                val title = binding.title.text.toString()
                val description = binding.desc.text.toString()

                val task = Task(title, description, "/camera/no_image.png")
                binding.title.setText("")
                binding.desc.setText("")
                binding.photoUrl.text = ""
                saveTask(task)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding = FragmentNewTaskBinding.inflate(inflater,container,false)

        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val photoBitmap = data?.extras?.get("data") as Bitmap
                // aquí puedes hacer algo con la foto, como mostrarla en un ImageView
                //taskViewModel.photoURL.value = binding.photoUrl.toString()
                binding.photoUrl.text = photoBitmap.toString()

                Log.i("BITMAP", photoBitmap.byteCount.toString())


            }

        }

        return binding.root
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageUri = it
        binding.imageView.setImageURI(it)
    }


    private fun saveTask(task: Task) = CoroutineScope(Dispatchers.IO).launch {
        val TodoTaskRef = firebaseFirestore.collection("Todo")
        try {
            TodoTaskRef.add(task).await()
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(),"Data guardada", Toast.LENGTH_LONG).show()
                dismiss()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(),e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImageDriveToStorage() {
        if (imageUri != null) {
            // Create a reference to the file in Firebase Storage

            val imageRef = storageRef.child("camera/${UUID.randomUUID()}.jpg")

            // Upload the file to Firebase Storage
            val uploadTask = imageRef.putFile(imageUri!!)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // If the upload is successful, get the download URL and save it to Firestore
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    taskViewModel.title.value = binding.title.text.toString()
                    taskViewModel.desc.value = binding.desc.text.toString()
                    val title = binding.title.text.toString()
                    val description = binding.desc.text.toString()


                    val task = Task(title, description, imageRef.path)
                    binding.title.setText("")
                    binding.desc.setText("")
                    binding.photoUrl.text = ""
                    saveTask(task)
                }
            }.addOnFailureListener {
                // If there is an error with the upload, display an error message
                Toast.makeText(requireContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}