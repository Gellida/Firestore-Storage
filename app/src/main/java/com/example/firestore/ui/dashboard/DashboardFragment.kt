package com.example.firestore.ui.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firestore.ui.task.TaskViewModel
import com.example.firestore.databinding.FragmentDashboardBinding
import com.example.firestore.ui.task.NewTaskFragment
import com.example.firestore.ui.task.TaskAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var taskViewModel: TaskViewModel
    private val taskList = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        initVars()

        taskRealtimeUpdates()

        delayShimmer()


        binding.fabAdd.setOnClickListener {
            NewTaskFragment().show(requireActivity().supportFragmentManager, "newTaskTag")
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskAdapter(taskList)
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION && position < taskList.size) {
                    val deletedTaskTitle = taskList[position].title
                    adapter.deleteTask(position, deletedTaskTitle)
                } else {
                    Log.i("error", taskList.toString())
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.adapter = adapter
    }

    private fun initVars() {
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }
    private fun taskRealtimeUpdates() {
        val todoTaskRef = firebaseFirestore.collection("Todo")
        val coroutineScope = CoroutineScope(Dispatchers.Main)

        todoTaskRef.addSnapshotListener { querySnapshot, error ->
            error?.let {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            querySnapshot?.let {
                coroutineScope.launch {
                    val tempList = withContext(Dispatchers.IO) {
                        val list = mutableListOf<Task>()
                        for (document in it) {
                            val task = document.toObject(Task::class.java)
                            list.add(task)
                        }
                        list
                    }
                    taskList.clear()
                    taskList.addAll(tempList)
                    adapter.updateData(taskList)
                }
            }
        }
    }


    private fun delayShimmer() {
        lifecycleScope.launch {
            delay(3000L)
            withContext(Dispatchers.Main) {
                showData()
            }
        }
    }

    private fun showData() {
        binding.viewLoading.isVisible = false
        binding.recyclerView.isVisible = true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        firebaseFirestore.clearPersistence()
    }
}