/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.datenightv3.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.adapter.IdeaAdapter
import com.example.datenightv3.databinding.IdeasFragmentBinding
import com.example.datenightv3.viewmodel.*
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class IdeasFragment: Fragment() {

    companion object {
        var IDEA_NAME = "ideaName"
    }

    private var _binding: IdeasFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private lateinit var ideaName: String

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val navigationArgs: IdeasFragmentArgs by navArgs()

    private val viewModel: AppViewModel by activityViewModels {
        AppViewModelFactory(
            (activity?.application as DatabaseApplication).categoryDatabase.categoryDao(),
            (activity?.application as DatabaseApplication).ideaDatabase.ideaDao()
        )
    }
    private val locationViewModel: LocationViewModel by activityViewModels {
        LocationViewModelFactory(
            (activity?.application as DatabaseApplication).locationDatabase.LocationDao()
        )
    }

    private lateinit var ideaAdapter: IdeaAdapter

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            ideaName = it.getString(IDEA_NAME).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = IdeasFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ideaAdapter = IdeaAdapter {
            val action = IdeasFragmentDirections
                .actionIdeasFragmentToIdeaDetailsFragment(
                    categoryName = it.categoryName,
                    ideaId = it.id
                )
            view.findNavController().navigate(action)
        }

        /*
        if (navigationArgs.categoryName == "Food") binding.ideaDistance.visibility = View.VISIBLE
        getLocation()
        //Distance feature needs to be fixed. Current method takes up too much resources on main thread
        */


        recyclerView.adapter = ideaAdapter
        bindView()
        setUpSearchBar()
    }

    private fun bindView() {
        lifecycle.coroutineScope.launch {
            viewModel.getIdeaInCategory(navigationArgs.categoryName).collect {
                /*for (idea in it) {
                    var distance: Double ?= null
                    if (navigationArgs.categoryName == "Food") {
                        val destLatitude = locationViewModel.getLocationLatitude(idea.ideaLocation)
                        val destLongitude =
                            locationViewModel.getLocationLongitude(idea.ideaLocation)
                        distance =
                            calcDistance(destLatitude, destLongitude, latitude, longitude)
                    }
                    distance = 1.0
                    viewModel.getUpdatedIdea(idea, distance)
                }*/
                ideaAdapter.submitList(it)
            }
        }
        binding.addIdeaButton.setOnClickListener {
            val action = IdeasFragmentDirections.actionIdeasFragmentToAddIdeaFragment(
                titleString = "Add New " + navigationArgs.categoryName + " Idea",
                categoryName = navigationArgs.categoryName
            )
            this.findNavController().navigate(action)
        }
    }

    private fun setUpSearchBar() {
        binding.ideaSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                lifecycle.coroutineScope.launch {
                    viewModel.queryIdea(query, navigationArgs.categoryName).collect {
                        ideaAdapter.submitList(it)
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                lifecycle.coroutineScope.launch {
                    viewModel.queryIdea(query, navigationArgs.categoryName).collect {
                        ideaAdapter.submitList(it)
                    }
                }
                return true
            }
        })
        //enable click anywhere on search bar
        binding.ideaSearchView.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                binding.ideaSearchView.setIconified(false)
            }
        })
    }

    private fun getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val locationRequest =
            LocationRequest().apply {
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                interval = TimeUnit.SECONDS.toMillis(10)
                fastestInterval = TimeUnit.SECONDS.toMillis(1)
            }
        val locationCallback = object : LocationCallback() {}

        checkLocationPermission()
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                this.latitude = location.latitude
                this.longitude = location.longitude
            }
        //change to getcurrentlocation
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

    private fun calcDistance(lat1: Double?, lon1: Double?, lat2: Double?, lon2: Double?): Double? {
        var d : Double ?= null
        if (lon2 != null && lat2 != null && lat1 != null && lon1 != null) {
            val R = 6371; // Radius of the earth in km
            val dLat = deg2rad(lat2 - lat1);  // deg2rad below
            val dLon = deg2rad(lon2 - lon1);
            val a =
                sin(dLat / 2) * sin(dLat / 2) +
                        cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                        sin(dLon / 2) * sin(dLon / 2)
            ;
            val c = 2 * atan2(sqrt(a), sqrt(1 - a));
            d = R * c; // Distance in km
        }
        return d
    }

    private fun deg2rad(deg: Double): Double {
        return deg * (Math.PI/180)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}