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
import android.util.Log
import android.view.*
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
import com.example.datenightv3.data.DataSorter
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.adapter.IdeaAdapter
import com.example.datenightv3.databinding.IdeasFragmentBinding
import com.example.datenightv3.viewmodel.*
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.*
import com.example.datenightv3.R

enum class sortType {
    NAME_ASCEND, NAME_DESCEND, DIST_ASCEND, DIST_DESCEND
}
class IdeasFragment: Fragment() {

    companion object {
        var IDEA_NAME = "ideaName"
    }

    private var _binding: IdeasFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val navigationArgs: IdeasFragmentArgs by navArgs()
    private lateinit var ideaName: String

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
    private var currentSort: sortType = sortType.NAME_ASCEND

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

        ideaAdapter = IdeaAdapter(latitude, longitude, navigationArgs.requireLocation) {
            val action = IdeasFragmentDirections
                .actionIdeasFragmentToIdeaDetailsFragment(
                    categoryName = viewModel.getCategoryName(it.categoryId),
                    ideaId = it.id,
                    requireLocation = navigationArgs.requireLocation
                )
            view.findNavController().navigate(action)
        }

        if (navigationArgs.requireLocation) binding.ideaDistanceLayout.visibility = View.VISIBLE
        recyclerView.adapter = ideaAdapter
        currentSort = sortType.NAME_ASCEND
        bindView()
        setUpSearchBar()
        initialiseRefreshListener(view)

    }

    private fun setAutoRefresh() {
        val handler = Handler()

        val refresh = Runnable {
            try {
                onViewCreated(binding.root, null)
            } catch (e: Exception) {
                setAutoRefresh()
            }
        }
        handler.postDelayed(refresh, 300)
    }

    private fun bindView() {
        bindList()
        binding.ideaNameLayout.setOnClickListener {
            if (currentSort == sortType.NAME_ASCEND) {
                currentSort = sortType.NAME_DESCEND
                binding.ideaNameSortArrow.visibility = View.VISIBLE
                binding.ideaNameSortArrow.setImageResource(R.drawable.ic_arrow_up)
                binding.ideaDistanceSortArrow.visibility = View.INVISIBLE
            }
            else {
                currentSort = sortType.NAME_ASCEND
                binding.ideaNameSortArrow.visibility = View.VISIBLE
                binding.ideaNameSortArrow.setImageResource(R.drawable.ic_arrow_down)
                binding.ideaDistanceSortArrow.visibility = View.INVISIBLE
            }
            bindList()
        }
        binding.ideaDistanceLayout.setOnClickListener {
            binding.ideaDistanceLayout
            if (currentSort == sortType.DIST_ASCEND) {
                currentSort = sortType.DIST_DESCEND
                ideaAdapter.submitList(ideaAdapter.currentList.reversed())
                binding.ideaDistanceSortArrow.visibility = View.VISIBLE
                binding.ideaDistanceSortArrow.setImageResource(R.drawable.ic_arrow_up)
                binding.ideaNameSortArrow.visibility = View.INVISIBLE
            } else {
                currentSort = sortType.DIST_ASCEND
                binding.ideaDistanceSortArrow.visibility = View.VISIBLE
                binding.ideaDistanceSortArrow.setImageResource(R.drawable.ic_arrow_down)
                binding.ideaNameSortArrow.visibility = View.INVISIBLE
            }
            bindList()
        }
    }

    private fun bindList() {
        ideaAdapter.submitList(null)
        lifecycle.coroutineScope.launch {
            viewModel.getIdeaInCategory(viewModel.getCategoryId(navigationArgs.categoryName)).collect {
                ideaAdapter.submitList(it)
                val dataSorter = DataSorter(it.toMutableList(),
                    ideaAdapter.distanceList,
                    viewModel.getIdeasCount(viewModel.getCategoryId(navigationArgs.categoryName)))
                if (currentSort == sortType.NAME_ASCEND) ideaAdapter.submitList(it)
                else if (currentSort == sortType.NAME_DESCEND) ideaAdapter.submitList(it.reversed())
                else if (currentSort == sortType.DIST_ASCEND) { ideaAdapter.submitList(dataSorter.sortDistance()) }
                else if (currentSort == sortType.DIST_DESCEND){ ideaAdapter.submitList(dataSorter.sortDistance().reversed()) }
            }
        }
    }

    private fun setUpSearchBar() {
        binding.ideaSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(query: String?): Boolean {
                lifecycle.coroutineScope.launch {
                    viewModel.queryIdea(query, viewModel.getCategoryId(navigationArgs.categoryName)).collect {
                        ideaAdapter.submitList(it)
                    }
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                lifecycle.coroutineScope.launch {
                    viewModel.queryIdea(query, viewModel.getCategoryId(navigationArgs.categoryName)).collect {
                        ideaAdapter.submitList(it)
                    }
                }
                binding.ideaSearchView.clearFocus()
                return true
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
            .addOnFailureListener {  }
            .addOnSuccessListener { location ->
                try {
                    this.latitude = location.latitude
                    this.longitude = location.longitude
                } catch (e: Exception) {
                    //Log.d("test", location.latitude.toString())
                }
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_button, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                lifecycle.coroutineScope.launch {
                    val categoryId = viewModel.getCategoryId(navigationArgs.categoryName)
                    val action = IdeasFragmentDirections.actionIdeasFragmentToAddIdeaFragment(
                        titleString = "Add New " + navigationArgs.categoryName + " Idea",
                        categoryId = categoryId,
                        requireLocation = navigationArgs.requireLocation
                    )
                    findNavController().navigate(action)
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}