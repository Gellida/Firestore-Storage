package com.example.firestore.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestore.ui.task.TaskViewModel
import com.example.firestore.databinding.FragmentDashboardBinding
import com.example.firestore.ui.task.NewTaskFragment
import com.example.firestore.ui.task.TaskAdapter
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseFirestore : FirebaseFirestore
    private var mList = mutableListOf<String>()
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

    }
    private fun TaskRealtimeUpdates(){
        val TodoTaskRef = firebaseFirestore.collection("Todo")
        TodoTaskRef.addSnapshotListener { querySnapshot, error ->
            error?.let {
                Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            querySnapshot?.let {
                val taskList = ArrayList<Task>()
                for (document in it){
                    val task = document.toObject(Task::class.java)
                    taskList.add(task)
                }
                binding.recyclerView.adapter = TaskAdapter(taskList)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}