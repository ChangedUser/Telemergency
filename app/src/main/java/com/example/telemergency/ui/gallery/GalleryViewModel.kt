package com.example.telemergency.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }

    private val _text2 = MutableLiveData<String>().apply {
        value = "This is another gallery Fragment"
    }
    val text: LiveData<String> = _text
    val text2: LiveData<String> = _text2
}