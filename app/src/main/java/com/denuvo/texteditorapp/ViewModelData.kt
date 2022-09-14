package com.denuvo.texteditorapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelData : ViewModel() {
   var task = MutableLiveData<String>()
   var desc = MutableLiveData<String>()
}