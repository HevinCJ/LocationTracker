package com.example.locationtracker.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.R
import com.example.locationtracker.databinding.FragmentMapsBinding
import com.example.locationtracker.viewmodel.MapsViewModel
import com.example.locationtracker.viewmodel.SearchViewmodel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapsFragment : Fragment(),OnMapReadyCallback {

    private var mapsFragment:FragmentMapsBinding?=null
    private val binding get() = mapsFragment!!

    private lateinit var supportMapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private val FINE_PERMISSION_CODE=101

    private val mapsViewModel:MapsViewModel by activityViewModels()

    private val searchViewmodel:SearchViewmodel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       mapsFragment = FragmentMapsBinding.inflate(inflater,container,false)
        initMap()

        return binding.root
    }

    private fun initMap() {
        supportMapFragment = childFragmentManager.findFragmentById(R.id.mapfrag) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavigationToSearchFrag()
        if (isPermissionsGranted()){
            if (isGpsEnabled()){
                mapsViewModel.requestLocation()

            }else{
                requestEnableGPS()
            }
        }else{
            requestPermissions()

        }

    }

    private fun setNavigationToSearchFrag() {

        binding.apply {
            floatingActionButton.setOnClickListener {

                if (isPermissionsGranted()){
                    if (isGpsEnabled()){
                        findNavController().navigate(R.id.action_mapsFragment_to_search)
                    }else{
                        requestEnableGPS()
                    }
                }else{
                    requestPermissions()
                }



            }
        }


    }


    private fun isPermissionsGranted():Boolean {
        return ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&   ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED
    }

    private fun requestRunTimePermissions(){

    }


     private fun requestPermissions(){
         ActivityCompat.requestPermissions(requireActivity(),arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),FINE_PERMISSION_CODE)
     }

    private fun isGpsEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun requestEnableGPS() {
        Toast.makeText(requireContext(), "Please enable GPS for location tracking", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

    override fun onMapReady(p0: GoogleMap) {

        googleMap = p0
        val uiSettings = googleMap.uiSettings
        uiSettings.isZoomControlsEnabled = true
        uiSettings.isZoomGesturesEnabled = true
        uiSettings.isCompassEnabled = true
        uiSettings.isMyLocationButtonEnabled = true
        uiSettings.isMapToolbarEnabled = true
        uiSettings.isRotateGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        uiSettings.isScrollGesturesEnabled = true

       observeCurrentLocation()
        observeCustomLocation()
        getMarkerAddress()
    }


    private fun getMarkerAddress() {
        googleMap.setOnMapLongClickListener {latlng->

            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(latlng.latitude,latlng.longitude,1,object :Geocoder.GeocodeListener{

                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            CoroutineScope(Dispatchers.IO).launch {
                                   addMarker(latlng, address)
                            }
                        }else{
                            Toast.makeText(requireContext(), "Unable to find address", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        Toast.makeText(requireContext(), "Failed to find address", Toast.LENGTH_SHORT).show()

                    }
                })
            }else{
                CoroutineScope(Dispatchers.IO).launch {
                    val addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)

                        if (addresses!!.isNotEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            Log.d("Customaddress", latlng.latitude.toString())
                            addMarker(latlng, address)

                        } else {
                            Toast.makeText(requireContext(), "Unable to find address", Toast.LENGTH_SHORT).show()
                        }


                }






            }


        }
    }

    private fun addMarker(latlng: LatLng, address: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            val markerOptions = MarkerOptions()
                .position(latlng)
                .title("Marked Location")
                .snippet(address)

            googleMap.addMarker(markerOptions)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15f))
        }


    }

    private fun observeCustomLocation() {
        searchViewmodel.customLocation.observe(viewLifecycleOwner){location->

            val customlocation = LatLng(location.latitude,location.longitude)

            if (customlocation.toString().isNotEmpty()){
                mapsViewModel.locationData.removeObservers(viewLifecycleOwner)
            }
            if (::googleMap.isInitialized){
                googleMap.addMarker(MarkerOptions().position(customlocation).title("Custom Search Location"))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customlocation, 10f))
            }

        }
    }

    private fun observeCurrentLocation() {
        mapsViewModel.locationData.observe(viewLifecycleOwner){location->
            location.let {
                val currentLocation = LatLng(it.latitude ?: 0.0,it.longitude ?: 0.0)
                googleMap.addMarker(MarkerOptions().position(currentLocation))
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))
            }
        }
    }




}