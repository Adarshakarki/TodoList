package com.adarsha.todolist

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.adarsha.todolist.databinding.FragmentNewTaskSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalTime

class NewTaskSheet(private var taskItem: TaskItem?) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNewTaskSheetBinding
    private lateinit var taskViewModel: TaskViewModel
    private var dueTime: LocalTime? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        taskViewModel = ViewModelProvider(activity)[TaskViewModel::class.java]

        taskItem?.let { task ->
            binding.taskTitle.setText(R.string.edit_task)
            val editable = Editable.Factory.getInstance()
            binding.name.text = editable.newEditable(task.name)
            binding.desc.text = editable.newEditable(task.desc)
            task.dueTime()?.let {
                dueTime = it
                updateTimeButtonText()
            }
        } ?: run {
            binding.taskTitle.setText(R.string.new_task)
        }

        binding.saveButton.setOnClickListener { saveAction() }
        binding.timePickerButton.setOnClickListener { openTimePicker() }
    }

    private fun openTimePicker() {
        dueTime = dueTime ?: LocalTime.now()
        val listener = TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            dueTime = LocalTime.of(selectedHour, selectedMinute)
            updateTimeButtonText()
        }
        TimePickerDialog(
            requireContext(),
            listener,
            dueTime?.hour ?: 0,
            dueTime?.minute ?: 0,
            true
        ).apply {
            setTitle(getString(R.string.task_due))
            show()
        }
    }

    private fun updateTimeButtonText() {
        binding.timePickerButton.text = String.format("%02d:%02d", dueTime!!.hour, dueTime!!.minute)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNewTaskSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun saveAction() {
        val name = binding.name.text.toString().trim()
        val desc = binding.desc.text.toString().trim()
        val dueTimeString = dueTime?.let { TaskItem.timeFormatter.format(it) }

        if (name.isNotEmpty()) {
            if (taskItem == null) {
                val newTask = TaskItem(name, desc, dueTimeString, null)
                taskViewModel.addTaskItem(newTask)
            } else {
                taskItem?.apply {
                    this.name = name
                    this.desc = desc
                    this.dueTimeString = dueTimeString
                    taskViewModel.updateTaskItem(this)
                }
            }
        }

        binding.name.setText("")
        binding.desc.setText("")
        dismiss()
    }
}
