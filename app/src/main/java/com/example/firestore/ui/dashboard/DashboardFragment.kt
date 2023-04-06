package com.example.firestore.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestore.NewTaskFragment
import com.example.firestore.TaskViewModel
import com.example.firestore.databinding.FragmentDashboardBinding
import com.example.firestore.databinding.FragmentNewTaskBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseFirestore : FirebaseFirestore
    private var mList = mutableListOf<String>()
    private lateinit var adapter: ImagesAdapter
    private val TAG = "DashboardFragment"

    private lateinit var taskViewModel: TaskViewModel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        initVars()


        dashboardViewModel.text.observe(viewLifecycleOwner) {
        }

        //se crea snapshot para traer datos en tiempo real
        TaskRealtimeUpdates()

        binding.fabAdd.setOnClickListener {
            NewTaskFragment().show(parentFragmentManager,"newTaskTag")
        }







        return root
    }
    private fun initVars(){
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ImagesAdapter(mList)
        binding.recyclerView.adapter = adapter

    }
    private fun TaskRealtimeUpdates(){
        val TodoTaskRef = firebaseFirestore.collection("Todo")
        TodoTaskRef.addSnapshotListener { querySnapshot, error ->
            error?.let {
                Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val sb = StringBuilder()
                for (document in it){
                    val task = document.toObject(Task::class.java)
                    sb.append("$task\n")
                }
                binding.titulo.text = sb.toString()
            }
        }
    }

    private fun getTasks() = CoroutineScope(Dispatchers.IO).launch {
        val TodoTaskRef = firebaseFirestore.collection("Todo")
        try {
            val querySnapshot = TodoTaskRef.get().await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents){
                val task = document.toObject(Task::class.java)
                sb.append("$task\n")
            }
            withContext(Dispatchers.Main){
                binding.titulo.text = sb.toString()
            }
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


        /*
        @SuppressLint("NotifyDataSetChanged")
    private fun getImages(){

        firebaseFirestore.collection("Images")
            .get().addOnSuccessListener {
                for (i in it){
                    mList.add(i.data["downloadUrl"].toString())
                }
                adapter.notifyDataSetChanged()
            }


         /*
        firebaseFirestore.collection("Tasks")
            .get().addOnSuccessListener {
                val taskList = mutableListOf<Task>()
                Log.i(TAG,it.toString())

                for (i in it){
                    val task = i.toObject(Task::class.java)
                    taskList.add(task)
                }
                adapter.updateData(taskList)
            }
        */

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/
}