package com.example.firestore.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.firestore.databinding.FragmentHomeBinding
import com.example.firestore.ui.task.DriveService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


private const val REQUEST_IMAGE_CAPTURE = 1;
private const val REQUEST_FROM_CAMERA = 1001;

class HomeFragment : Fragment() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var storageRef: StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var imageUri: Uri? = null

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


        //Botón para guardar una imagen precargada con drive
        binding.btnSave.setOnClickListener {
            uploadImageDriveToStorage()
        }


        binding.btnRecover.setOnClickListener {
            db.collection("Tasks").document(binding.titulo.text.toString()).get()
                .addOnSuccessListener {
                    binding.descripcion.setText(it.get("description") as String?)
                }
        }
        binding.btnEliminar.setOnClickListener {
            db.collection("Tasks").document(binding.titulo.text.toString()).delete()
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
            val intent = Intent(requireContext(), DriveService::class.java).apply {
                putExtra("imageUri", imageUri)
            }
            requireActivity().startService(intent)
        }
    }



}