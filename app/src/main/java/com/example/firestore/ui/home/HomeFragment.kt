package com.example.firestore.ui.home

import android.Manifest
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.firestore.databinding.FragmentHomeBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.util.*


private const val REQUEST_IMAGE_CAPTURE = 1;
private const val REQUEST_FROM_CAMERA = 1001;

class HomeFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var imageUri: Uri? = null
    private var photoUri: Uri? = null

    private val TAG = "HomeFragment"


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


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
    private fun initVars() {
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerClickEvents() {
        binding.imageView.setOnClickListener {
            resultLauncher.launch("image/*")
        }

        binding.ivCamera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, podemos tomar la foto
                takePhoto()
            } else {
                // Permiso no concedido, solicitar permiso
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_FROM_CAMERA)
            }
        }


        //Botón para guardar una imagen precargada con drive
        binding.btnSave.setOnClickListener {
            uploadImageDriveToStorage()
        }

        binding.btnService.setOnClickListener {
            val intent = Intent(requireContext(), MyService::class.java)
            activity?.startService(intent)
        }
        binding.btnServiceStop.setOnClickListener {
            val intent = Intent(requireContext(), MyService::class.java)
            activity?.stopService(intent)
        }

        binding.btnRecover.setOnClickListener {
            db.collection("Tasks").document(binding.titulo.text.toString()).get()
                .addOnSuccessListener {
                    binding.titulo.setText(it.get("nombre") as String?)
                    binding.descripcion.setText(it.get("descripcion") as String?)
                }
        }
        binding.btnEliminar.setOnClickListener {
            db.collection("Tasks").document(binding.titulo.text.toString()).delete()
        }
    }
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }


    // Método para recibir la imagen capturada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            // Aquí puedes guardar la imagen en el Storage de Firebase
            Toast.makeText(requireContext(),"Si se llegó hasta antes del guardado",Toast.LENGTH_LONG).show()
            binding.ivCamera.setImageBitmap(imageBitmap)
            uploadImageToFirebase(imageBitmap)
        }
    }
    private fun uploadImageToFirebase(bitmap: Bitmap) {
        // Crea una referencia al archivo en el almacenamiento de Firebase
        val imageRef = storageRef.child("camera/${UUID.randomUUID()}.jpg")

        // Convierte el Bitmap en bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Sube la imagen a Firebase
        val uploadTask = imageRef.putBytes(data)
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

                firebaseFirestore.collection("Tasks").document("task"+binding.titulo.text.toString())
                    .set(task)
                    .addOnSuccessListener {
                        Toast.makeText(requireActivity(),"Guardado exitosamente",Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error adding document", e)
                    }
            }?.addOnFailureListener { exception ->
                // Handle any errors
            }


        }.addOnFailureListener {
            // Si hay algún error en la subida, muestra un mensaje de error
            Toast.makeText(requireContext(), "Error al subir la imagen.", Toast.LENGTH_SHORT).show()
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        imageUri = it
        binding.imageView.setImageURI(it)
    }


    private fun uploadImageDriveToStorage() {
        if (imageUri != null) {
            val imagesRef = storageRef.child("drive/${imageUri!!.lastPathSegment}")
            val uploadTask = imagesRef.putFile(imageUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "Image uploaded successfully: ${taskSnapshot.metadata?.path}")
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "URL: $uri")

                    //Upload image to firestore with path
                    val task = hashMapOf(
                        "title" to binding.titulo.text.toString(),
                        "description" to binding.descripcion.text.toString(),
                        "downloadUrl" to uri.toString()
                    )

                    firebaseFirestore.collection("Tasks").document("task"+binding.titulo.text.toString())
                        .set(task)
                        .addOnSuccessListener {
                            Toast.makeText(requireActivity(),"Guardado exitosamente",Toast.LENGTH_LONG).show()
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


}