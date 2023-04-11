package com.example.firestore

import android.app.appsearch.AppSearchResult.RESULT_OK
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.get
import androidx.core.view.drawToBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.firestore.databinding.FragmentNewTaskBinding
import com.example.firestore.ui.dashboard.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity).get(TaskViewModel::class.java)

        binding.btnPhoto.setOnClickListener {
            /*
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
             */
            takePhoto()

        }
        binding.btnSave.setOnClickListener{
            uploadImageToFirebase(binding.photoUrl.drawToBitmap())
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

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val wrapper = ContextWrapper(context)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            //uploadImageToFirebase(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun takePhoto(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoLauncher.launch(takePictureIntent)
    }

    private fun saveAction(){
        taskViewModel.title.value = binding.title.text.toString()
        taskViewModel.desc.value = binding.desc.text.toString()
        val title = binding.title.text.toString()
        val description = binding.desc.text.toString()
        val photoURL = "as"
        //binding.title.setText("")
        //binding.desc.setText("")
        val task = Task(title,description,photoURL)
        saveTask(task)

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

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        // Crea una referencia al archivo en el almacenamiento de Firebase
        val imageRef = storageRef.child("camera/${UUID.randomUUID()}.jpg")


        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->

            val downloadUrl = taskSnapshot.metadata?.reference?.path

            taskViewModel.title.value = binding.title.text.toString()
            taskViewModel.desc.value = binding.desc.text.toString()
            val title = binding.title.text.toString()
            val description = binding.desc.text.toString()
            val imageRef = storageRef.child("camera/$downloadUrl")


            val task = Task(title,description,downloadUrl.toString())
            binding.title.setText("")
            binding.desc.setText("")
            binding.photoUrl.text = ""
            saveTask(task)

        }.addOnFailureListener {
            // Si hay algún error en la subida, muestra un mensaje de error
            Toast.makeText(requireContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show()
        }
    }

}