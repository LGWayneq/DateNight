package com.example.datenightv3.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.R
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.databinding.MapFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MapFragment : Fragment() {

    private var _binding : MapFragmentBinding ?= null
    private val binding get() = _binding!!

    lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private val navigationArgs: MapFragmentArgs by navArgs()

    private val locationViewModel: LocationViewModel by activityViewModels {
        LocationViewModelFactory(
            (activity?.application as DatabaseApplication).locationDatabase.LocationDao()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = MapFragmentBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.onResume() //need to get map to display immediately

        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            //Log.d("test",e.toString())
        }
        checkLocationPermission()
        mapView.getMapAsync(OnMapReadyCallback {
            it.setMyLocationEnabled(true)

            lifecycle.coroutineScope.launch {
                val marker = LatLng(
                    locationViewModel.getLocationLatitude(navigationArgs.locationName)!!,
                    locationViewModel.getLocationLongitude(navigationArgs.locationName)!!
                )
                it.addMarker(MarkerOptions().position(marker).title(navigationArgs.ideaName))

                val cameraPosition = CameraPosition.Builder().target(marker).zoom(15F).build()
                it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }
    }
}