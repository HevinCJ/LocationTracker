package com.example.locationtracker.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


class MapsViewModel(application: Application):AndroidViewModel(application) {

    private val fusedLocationProviderClient:FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(application)
    }

    private val _locationData = MutableLiveData<Location>()
    val locationData: LiveData<Location> = _locationData



   @SuppressLint("MissingPermission")
   fun requestLocation(){
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
           fusedLocationProviderClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY,createCancellationToken()).addOnSuccessListener { currentLocation->
               currentLocation.let {
                   _locationData.postValue(it)
               }
           }
       }else{
           fusedLocationProviderClient.lastLocation.addOnSuccessListener { currentLocation ->
                     currentLocation?.let { _locationData.postValue(it) }
                  }
       }
   }



    private fun createCancellationToken(): CancellationToken {
        return object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                return CancellationTokenSource().token
            }

            override fun isCancellationRequested(): Boolean {
                return false
            }
        }
    }


}