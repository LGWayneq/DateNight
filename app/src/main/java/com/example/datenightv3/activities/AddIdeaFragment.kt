package com.example.datenightv3.activities

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.WebInterface
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.AddIdeaFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

class AddIdeaFragment : Fragment() {

    private var _binding: AddIdeaFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: AddIdeaFragmentArgs by navArgs()

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
    private lateinit var webInterface: WebInterface

    private lateinit var idea: Idea
    private lateinit var locations: List<String>
    private var latitude: Double ?= null
    private var longitude: Double ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = AddIdeaFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (navigationArgs.requireLocation) {
            binding.ideaLocation.visibility = View.VISIBLE
            binding.ideaLocationLabel.visibility = View.VISIBLE
        }

        locationViewModel.getAllLocationNames().observe(this.viewLifecycleOwner) {
            locations = it
            bindAutoComplete()
        }
        val id = navigationArgs.ideaId
        if (id > 0) {
            viewModel.getIdea(id).observe(this.viewLifecycleOwner) {
                idea = it
                bindEditText(idea)
            }
        } else {
            webInterface = WebInterface()
            binding.saveAction.setOnClickListener {
                lifecycle.coroutineScope.launch { addNewIdea() }
            }
            binding.ideaName.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    lifecycle.coroutineScope.launch {
                        val locationName = webInterface.getLocationName(binding.ideaName.text.toString())
                        if (locationName != null) binding.ideaLocation.setText(locationName, TextView.BufferType.SPANNABLE)
                    }
                }
            }
        }
    }

    private fun bindAutoComplete() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,  locations)
        binding.ideaLocation.setAdapter(adapter)
    }

    private fun bindEditText(idea: Idea) {
        binding.apply {
            ideaName.setText(idea.name, TextView.BufferType.SPANNABLE)
            lifecycle.coroutineScope.launch {
                val locationName = locationViewModel.getLocationName(idea.locationId)
                ideaLocation.setText(locationName, TextView.BufferType.SPANNABLE)
            }
            ideaDescription.setText(idea.description, TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener {
                lifecycle.coroutineScope.launch {updateIdea()}
            }
        }
    }

    private suspend fun addNewIdea() {
        if (isEntryValid()) {
            if (navigationArgs.requireLocation){
                var locationId = locationViewModel.getLocationId(binding.ideaLocation.text.toString())
                if (locationId == null) locationId = locationViewModel.addLocation(binding.ideaLocation.text.toString(), latitude!!, longitude!!)
                viewModel.addIdea(
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryId,
                    binding.ideaDescription.text.toString(),
                    locationId,
                    locationViewModel.getLocationLatitude(locationId),
                    locationViewModel.getLocationLongitude(locationId)
                )
                findNavController().navigateUp()
            } else {
                viewModel.addIdea(
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryId,
                    binding.ideaDescription.text.toString(),
                    null
                )
                this.findNavController().navigateUp()
            }
        }
    }

    private suspend fun updateIdea() {
        if (isEntryValid()) {
            if (navigationArgs.requireLocation) {
                val locationId = locationViewModel.getLocationId(binding.ideaLocation.text.toString())
                viewModel.getUpdatedIdea(
                    navigationArgs.ideaId,
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryId,
                    binding.ideaDescription.text.toString(),
                    locationId,
                    locationViewModel.getLocationLatitude(locationId),
                    locationViewModel.getLocationLongitude(locationId)
                )
            } else {
                viewModel.getUpdatedIdea(
                    navigationArgs.ideaId,
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryId,
                    binding.ideaDescription.text.toString(),
                    null
                )
            }
            this.findNavController().navigateUp()
        }
    }

    private suspend fun isEntryValid(): Boolean {
        if (!navigationArgs.requireLocation) return !(binding.ideaName.text.toString().isBlank() || binding.ideaDescription.text.toString().isBlank())
        else {
            if (binding.ideaLocation.text.toString() !in locations) {
                val coordinateList = webInterface.getLocationCoordinates(binding.ideaLocation.text.toString())
                latitude = coordinateList[0]
                longitude = coordinateList[1]
                if (latitude == null || latitude == 45.0 || longitude == null || longitude == -90.0) {
                    binding.ideaLocationWarning.visibility = View.VISIBLE
                    return false
                }
            }
            return !(binding.ideaName.text.toString().isBlank() ||
                    binding.ideaLocation.text.toString().isBlank() ||
                    binding.ideaDescription.text.toString().isBlank())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)

        _binding = null
    }
}