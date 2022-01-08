package com.example.datenightv3.activities

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.R
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.data.classes.Location
import com.example.datenightv3.databinding.AddIdeaFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory

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

    private lateinit var idea: Idea
    lateinit var locations: List<String>

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

        val id = navigationArgs.ideaId
        if (navigationArgs.categoryName == "Food") {
            binding.ideaLocation.visibility = View.VISIBLE
            binding.ideaLocationLabel.visibility = View.VISIBLE
        }

        locationViewModel.getAllLocationNames().observe(viewLifecycleOwner, Observer {
            locations = it
            bindAutoComplete()
        })

        if (id > 0) {
            viewModel.getIdea(id).observe(this.viewLifecycleOwner) {
                idea = it
                bindEditText(idea)
            }
        } else {
            binding.saveAction.setOnClickListener { addNewIdea() }
        }

    }

    private fun bindAutoComplete() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1,  locations)
        binding.ideaLocation.setAdapter(adapter)
    }

    private fun bindEditText(idea: Idea) {
        binding.apply {
            ideaName.setText(idea.ideaName, TextView.BufferType.SPANNABLE)
            ideaLocation.setText(idea.ideaLocation, TextView.BufferType.SPANNABLE)
            ideaDescription.setText(idea.ideaDescription, TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateIdea() }
        }
    }

    private fun addNewIdea() {
        if (isEntryValid()) {
            if (navigationArgs.categoryName == "Food"){
                viewModel.addIdea(
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryName,
                    binding.ideaDescription.text.toString(),
                    binding.ideaLocation.text.toString()
                )
            } else {
                viewModel.addIdea(
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryName,
                    binding.ideaDescription.text.toString(),
                    null
                )
            }
            this.findNavController().navigateUp()
        }
    }

    private fun updateIdea() {
        if (isEntryValid()) {
            if (navigationArgs.categoryName == "Food") {
                viewModel.getUpdatedIdea(
                    navigationArgs.ideaId,
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryName,
                    binding.ideaDescription.text.toString(),
                    binding.ideaLocation.text.toString(),
                    null
                )
            } else {
                viewModel.getUpdatedIdea(
                    navigationArgs.ideaId,
                    binding.ideaName.text.toString(),
                    navigationArgs.categoryName,
                    binding.ideaDescription.text.toString(),
                    null,
                    null
                )
            }
            this.findNavController().navigateUp()
        }
    }

    private fun isEntryValid(): Boolean {
        if (navigationArgs.categoryName != "Food") return !(binding.ideaName.text.toString().isBlank() || binding.ideaDescription.text.toString().isBlank())
        return !(binding.ideaName.text.toString().isBlank() ||
                binding.ideaLocation.text.toString().isBlank() ||
                binding.ideaDescription.text.toString().isBlank()) && binding.ideaLocation.text.toString() in locations
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