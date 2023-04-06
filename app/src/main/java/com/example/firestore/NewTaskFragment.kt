package com.example.firestore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.firestore.databinding.FragmentNewTaskBinding
import com.example.firestore.ui.dashboard.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class NewTaskFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNewTaskBinding
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var firebaseFirestore : FirebaseFirestore


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity).get(TaskViewModel::class.java)

        binding.btnSave.setOnClickListener{
            saveAction()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding = FragmentNewTaskBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun saveAction(){
        taskViewModel.title.value = binding.title.text.toString()
        taskViewModel.desc.value = binding.desc.text.toString()
        val title = binding.title.text.toString()
        val description = binding.desc.text.toString()
        binding.title.setText("")
        binding.desc.setText("")
        val photoURL = "PATHGS://"
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

}