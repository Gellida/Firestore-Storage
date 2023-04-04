package com.example.firestore.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.Manifest
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.example.firestore.R
import com.example.firestore.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import kotlin.math.log

private const val REQUEST_IMAGE_CAPTURE = 1

class HomeFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storageRef : StorageReference
    private lateinit var firebaseFirestore : FirebaseFirestore
    private var imageUri : Uri? = null

    private val TAG = "HomeFragment"



    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
                binding.imageView2.setImageBitmap(imageBitmap)

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.titulo
        initVars()
        registerClickEvents()

        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerClickEvents(){
        binding.btnCamera.setOnClickListener {
            uploadImage()
        }
        binding.imageView2.setOnClickListener {
            if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            } else {
                dispatchTakePictureIntent()
            }
        }

        binding.btnRecover.setOnClickListener {
            db.collection("Tasks").document(binding.titulo.text.toString()).get().addOnSuccessListener {
                binding.titulo.setText(it.get("nombre") as String?)
                binding.descripcion.setText(it.get("descripcion") as String?)
            }
        }
        binding.btnEliminar.setOnClickListener {
            db.collection("Tasks").document(binding.titulo.text.toString()).delete()
        }
        binding.imageView.setOnClickListener {
            resultLauncher.launch("image/*")
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()){
        imageUri = it
        binding.imageView.setImageURI(it)
    }

    private fun initVars(){
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(takePictureIntent)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Log.i(TAG,"Permisos denegados")
            }
        }
    }


    private fun compressFile(imageBitmap: Bitmap?) {
        val filename = "img"+binding.titulo
        val file = File(requireActivity().getExternalFilesDir(null), filename)
        val outputStream = FileOutputStream(file)
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        Log.d(TAG,file.toString())
        val imageRef = storageRef.child("images/$filename")
        val uploadTask = imageRef.putFile(Uri.fromFile(file))


        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl

        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d(TAG, "File uploaded successfully: $downloadUri")
                val map = HashMap<String, Any > ()
                map["picture"] = downloadUri

                firebaseFirestore.collection("Images").add(map).addOnCompleteListener { firestoreTask ->
                    if (firestoreTask.isSuccessful){
                        Log.i(TAG,"Se hizo exitosamente")

                        db.collection("Tasks").document(
                            binding.titulo.text.toString()).set(
                            hashMapOf("nombre" to binding.titulo.text.toString(),
                                "descripcion" to binding.descripcion.text.toString(),
                                "Path" to downloadUri.toString())
                        )
                    } else {
                        Log.i(TAG,"Error en la subida de los datos")
                    }



                    binding.imageView.setImageResource(R.drawable.ic_success)

                }
            } else {
                Log.e(TAG, "Failed to upload file.", task.exception)
            }
        }
    }

    private fun uploadImage(){
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        imageUri?.let {
            storageRef.putFile(it).addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val map = HashMap<String, Any > ()
                        map["picture"] = uri.toString()

                        firebaseFirestore.collection("Images").add(map).addOnCompleteListener { firestoreTask ->
                            if (firestoreTask.isSuccessful){
                                Log.i(TAG, "Se hizo exitosamente$uri")

                                db.collection("Tasks").document(
                                    binding.titulo.text.toString()).set(
                                    hashMapOf("nombre" to binding.titulo.text.toString(),
                                        "descripcion" to binding.descripcion.text.toString(),
                                        "Path" to uri.toString())
                                )
                            } else {
                                Log.i(TAG,"Error en la subida de los datos")
                            }



                            binding.imageView.setImageResource(R.drawable.ic_success)

                        }

                    }
                } else {
                    Log.i(TAG,"Error al intentar manipular el archivo")
                    binding.imageView.setImageResource(R.drawable.ic_success)
                }

            }
        }
    }


}