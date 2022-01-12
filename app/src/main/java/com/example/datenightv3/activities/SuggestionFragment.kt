package com.example.datenightv3.activities

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.R
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.SuggestionFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory
import com.example.datenightv3.viewmodel.LocationViewModel
import com.example.datenightv3.viewmodel.LocationViewModelFactory
import kotlinx.coroutines.launch

class SuggestionFragment : Fragment() {

    private var _binding : SuggestionFragmentBinding? = null
    private val binding get() = _binding!!

    private val navigationArgs: SuggestionFragmentArgs by navArgs()

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
    private var suggestion : Idea ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SuggestionFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindSuggestion()
        if (navigationArgs.requireLocation) {
            binding.ideaLocation.visibility = View.VISIBLE
            binding.ideaLocationLabel.visibility = View.VISIBLE
            binding.locateButton.visibility = View.VISIBLE
            binding.locateButton.setOnClickListener {
                val action = SuggestionFragmentDirections.actionSuggestionFragmentToMapFragment(
                    suggestion!!.name,
                    suggestion!!.locationId!!
                )
                findNavController().navigate(action)
            }
        }
        binding.filterButton.setOnClickListener { } //Filter not yet implemented
        binding.rerollButton.setOnClickListener { bindSuggestion() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_item, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.setType("text/plain")
                if (navigationArgs.requireLocation) lifecycle.coroutineScope.launch {
                    val locationName = locationViewModel.getLocationName(suggestion?.locationId)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text,
                        suggestion?.name,
                        locationName,
                        suggestion?.description)
                    )
                }
                else shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text_no_location,
                    suggestion?.name,
                    suggestion?.description)
                )
                startActivity(Intent.createChooser(shareIntent, "Share using"))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun bindSuggestion() {
        viewModel.generateSuggestion(navigationArgs.categoryId).observe(this.viewLifecycleOwner) { newIdea ->
            if (suggestion == null || suggestion!!.name != newIdea.name) {
                this.suggestion = newIdea
                binding.apply {
                    ideaName.text = suggestion?.name
                    lifecycle.coroutineScope.launch {
                        ideaLocation.text = locationViewModel.getLocationName(suggestion?.locationId)
                    }
                    ideaDescription.text = suggestion?.description
                }
            } else {
                bindSuggestion()
            }
        }
    }

}