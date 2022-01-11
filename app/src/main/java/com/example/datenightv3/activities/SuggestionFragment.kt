package com.example.datenightv3.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.datenightv3.data.DatabaseApplication
import com.example.datenightv3.data.classes.Idea
import com.example.datenightv3.databinding.SuggestionFragmentBinding
import com.example.datenightv3.viewmodel.AppViewModel
import com.example.datenightv3.viewmodel.AppViewModelFactory

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

    private var suggestion : Idea ?= null

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
                    suggestion!!.ideaName,
                    suggestion!!.ideaLocation!!
                )
                findNavController().navigate(action)
            }
        }
        binding.filterButton.setOnClickListener { } //Filter not yet implemented
        binding.rerollButton.setOnClickListener { bindSuggestion() }
    }

    private fun bindSuggestion() {
        viewModel.generateSuggestion(navigationArgs.categoryName).observe(this.viewLifecycleOwner) { newIdea ->
            if (suggestion == null || suggestion!!.ideaName != newIdea.ideaName) {
                this.suggestion = newIdea
                binding.apply {
                    ideaName.text = suggestion?.ideaName
                    ideaLocation.text = suggestion?.ideaLocation
                    ideaDescription.text = suggestion?.ideaDescription
                }
            } else {
                bindSuggestion()
            }
        }
    }

}