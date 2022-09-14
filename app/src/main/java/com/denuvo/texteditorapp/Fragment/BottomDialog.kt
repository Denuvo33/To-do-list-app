package com.denuvo.texteditorapp.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.lifecycle.ViewModelProvider
import com.denuvo.texteditorapp.MainActivity
import com.denuvo.texteditorapp.ViewModelData
import com.denuvo.texteditorapp.databinding.FragmentBottomDialogBinding
import com.denuvo.texteditorapp.recycler.TaskData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson

class BottomDialog: BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomDialogBinding
    private lateinit var viewModelData: ViewModelData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        viewModelData = ViewModelProvider(activity).get(ViewModelData::class.java)
        binding.checkRemindme.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                binding.timeLayout.visibility = View.VISIBLE
            } else{
                binding.timeLayout.visibility = View.GONE
            }
        }


        binding.createTask.setOnClickListener {
            if (binding.edtTask.text!!.isNotEmpty()) {
                saveAction()
                Log.d("Task","Task created")
            }else {
                Toast.makeText(activity,"Create Task First",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAction() {
        viewModelData.task.value = binding.edtTask.text.toString()
        viewModelData.desc.value = binding.edtTitle.text.toString()
        Log.d("TAG","Desc value:${viewModelData.desc.value}")
        binding.edtTitle.text!!.clear()
        binding.edtTask.text!!.clear()
        dismiss()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBottomDialogBinding.inflate(inflater,container,false)
        return binding.root
    }
}