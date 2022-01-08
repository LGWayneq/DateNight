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
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*

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
        setAutoRefresh()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getLocation()

        ideaAdapter = IdeaAdapter(latitude, longitude) {
            val action = IdeasFragmentDirections
                .actionIdeasFragmentToIdeaDetailsFragment(
                    categoryName = it.categoryName,
                    ideaId = it.id
                )
            view.findNavController().navigate(action)
        }

        if (navigationArgs.categoryName == "Food") binding.ideaDistance.visibility = View.VISIBLE

        recyclerView.adapter = ideaAdapter

        bindView()
        setUpSearchBar()
        initialiseRefreshListener(view)

    }

    private fun setAutoRefresh() {
        val handler = Handler()

        //val currentTime = SystemClock.currentThreadTimeMillis()
        val refresh = Runnable {
            onViewCreated(binding.root, null)
        }
        handler.postDelayed(refresh, 300)
            }

    private fun bindView() {
        Log.d("MyActivity", "Binding view")
        lifecycle.coroutineScope.launch {
            viewModel.getIdeaInCategory(navigationArgs.categoryName).collect {
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

    private fun initialiseRefreshListener(view: View) {
        val swipeRefreshLayout=binding.swipeLayout
        swipeRefreshLayout.setOnRefreshListener { // This method gets called when user pull for refresh,
            onViewCreated(view, null)
            val handler = Handler()
            handler.postDelayed(Runnable {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false)
                }
            }, 700)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}