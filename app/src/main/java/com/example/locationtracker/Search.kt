package com.example.locationtracker

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.databinding.FragmentSearchBinding
import com.example.locationtracker.viewmodel.SearchViewmodel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.NumberFormatException

class Search : Fragment() {
    private var search:FragmentSearchBinding?=null
    private val binding get() = search!!

   private val searchViewmodel:SearchViewmodel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        search = FragmentSearchBinding.inflate(inflater,container,false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.apply {
            searchbtn.setOnClickListener {
                val latitudeStr = edttextlatitude.text.toString().trim()
                val longitudeStr = edttextlongitude.text.toString().trim()


                if (latitudeStr.isNotEmpty()&&longitudeStr.isNotEmpty()){

                    try {
                        val latitude = latitudeStr.toDouble()
                        val longitude = longitudeStr.toDouble()

                        val customLocation = LatLng(latitude,longitude)
                        Log.d("inputlocation","${customLocation.longitude},${customLocation.latitude}")
                        searchViewmodel.setCustomLocation(customLocation)

                       findNavController().navigate(R.id.action_search_to_mapsFragment)

                    }catch (e:NumberFormatException){
                        Toast.makeText(requireContext(), "Invalid latitude or longitude", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), "Please fill required fields", Toast.LENGTH_SHORT).show()
                }


            }
        }
    }




}