package com.example.locationtracker.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class SearchViewmodel:ViewModel() {

    private var _customLocation = MutableLiveData<LatLng>()
    val customLocation: LiveData<LatLng> get() = _customLocation



    fun setCustomLocation(latLng: LatLng) {
        _customLocation.postValue(latLng)
    }



}